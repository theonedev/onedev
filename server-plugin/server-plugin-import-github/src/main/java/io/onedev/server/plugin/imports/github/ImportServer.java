package io.onedev.server.plugin.imports.github;

import com.fasterxml.jackson.databind.JsonNode;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.bootstrap.SensitiveMasker;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.entitymanager.*;
import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.issue.IssuesImported;
import io.onedev.server.git.command.LsRemoteCommand;
import io.onedev.server.model.*;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.JerseyUtils.PageDataConsumer;
import io.onedev.server.util.Pair;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.taskbutton.TaskResult.HtmlMessgae;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.authz.UnauthorizedException;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Editable
@ClassValidating
public class ImportServer implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ImportServer.class);
	
	private static final int PER_PAGE = 50;
	
	private static final String PROP_API_URL = "apiUrl";
	
	private static final String PROP_ACCESS_TOKEN = "accessToken";
	
	private String apiUrl = "https://api.github.com";
	
	private String accessToken;
	
	@Editable(order=10, name="GitHub API URL", description="Specify GitHub API url, for instance <tt>https://api.github.com</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Editable(order=100, name="GitHub Personal Access Token", description="GitHub personal access token should be generated with "
			+ "scope <b>repo</b> and <b>read:org</b>")
	@Password
	@NotEmpty
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	private String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}
	
	List<String> listOrganizations() {
		List<String> organizations = new ArrayList<>();
		
		Client client = newClient();
		try {
			String apiEndpoint = getApiEndpoint("/user/orgs");
			for (JsonNode orgNode: list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			})) {
				organizations.add(orgNode.get("login").asText());
			}	
			Collections.sort(organizations);
		} catch (Exception e) {
			logger.error("Error listing organizations", e);
		} finally {
			client.close();
		}
		
		return organizations;
	}
	
	List<String> listRepositories(@Nullable String organization, boolean includeForks) {
		Client client = newClient();
		try {
			String apiEndpoint;
			if (organization != null) 
				apiEndpoint = getApiEndpoint("/orgs/" + organization + "/repos");
			else 
				apiEndpoint = getApiEndpoint("/user/repos?type=owner");
			List<String> repositories = new ArrayList<>();
			for (JsonNode repoNode: list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			})) {
				String repoName = repoNode.get("name").asText();
				String ownerName = repoNode.get("owner").get("login").asText();
				if (includeForks || !repoNode.get("fork").asBoolean())
					repositories.add(ownerName + "/" + repoName);
			}					
			Collections.sort(repositories);
			return repositories;
		} finally {
			client.close();
		}
		
	}

	private Client newClient() {
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		client.register(HttpAuthenticationFeature.basic("git", getAccessToken()));
		return client;
	}
	
	IssueImportOption buildIssueImportOption(Collection<String> gitHubRepos) {
		IssueImportOption importOption = new IssueImportOption();
		Client client = newClient();
		try {
			TaskLogger taskLogger = new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			};
			Set<String> labels = new LinkedHashSet<>();
			for (String each: gitHubRepos) {
				String apiEndpoint = getApiEndpoint("/repos/" + each + "/labels"); 
				for (JsonNode labelNode: list(client, apiEndpoint, taskLogger)) 
					labels.add(labelNode.get("name").asText());
			}
			
			for (String label: labels) {
				IssueLabelMapping mapping = new IssueLabelMapping();
				mapping.setGitHubIssueLabel(label);
				importOption.getIssueLabelMappings().add(mapping);
			}
		} finally {
			client.close();
		}
		return importOption;
	}
	
	@Nullable
	private Long getUserId(Client client, Map<String, Optional<Long>> userIds,
						   String login, TaskLogger logger) {
		Optional<Long> userIdOpt = userIds.get(login);
		if (userIdOpt == null) {
			String apiEndpoint = getApiEndpoint("/users/" + login);
			String email = get(client, apiEndpoint, logger).get("email").asText(null);
			if (email != null) 
				userIdOpt = Optional.ofNullable(User.idOf(OneDev.getInstance(UserManager.class).findByVerifiedEmailAddress(email)));
			else 
				userIdOpt = Optional.empty();
			userIds.put(login, userIdOpt);
		}
		return userIdOpt.orElse(null);
	}
	
	ImportResult importIssues(String gitHubRepo, Project oneDevProject, IssueImportOption importOption, 
			Map<String, Optional<Long>> userIds, boolean dryRun, TaskLogger logger) {
		Client client = newClient();
		IssueManager issueManager = OneDev.getInstance(IssueManager.class);
		try {
			Set<String> nonExistentMilestones = new HashSet<>();
			Set<String> nonExistentLogins = new HashSet<>();
			Set<String> unmappedIssueLabels = new HashSet<>();
			
			Map<String, Pair<FieldSpec, String>> labelMappings = new HashMap<>();
			Map<String, Milestone> milestoneMappings = new HashMap<>();
			
			for (IssueLabelMapping mapping: importOption.getIssueLabelMappings()) {
				String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
				String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				labelMappings.put(mapping.getGitHubIssueLabel(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			
			for (Milestone milestone: oneDevProject.getMilestones())
				milestoneMappings.put(milestone.getName(), milestone);
			
			String initialIssueState = getIssueSetting().getInitialStateSpec().getName();
				
			List<Issue> issues = new ArrayList<>();
			
			Map<Long, Long> issueNumberMappings = new HashMap<>();
			
			AtomicInteger numOfImportedIssues = new AtomicInteger(0);
			PageDataConsumer pageDataConsumer = new PageDataConsumer() {

				private String joinAsMultilineHtml(List<String> values) {
					List<String> escapedValues = new ArrayList<>();
					for (String value: values)
						escapedValues.add(HtmlEscape.escapeHtml5(value));
					return StringUtils.join(escapedValues, "<br>");
				}
				
				@Override
				public void consume(List<JsonNode> pageData) throws InterruptedException {
					for (JsonNode issueNode: pageData) {
						if (Thread.interrupted())
							throw new InterruptedException();
						
						if (issueNode.get("pull_request") != null)
							continue;

						Map<String, String> extraIssueInfo = new LinkedHashMap<>();
						
						Issue issue = new Issue();
						issue.setProject(oneDevProject);
						issue.setTitle(issueNode.get("title").asText());
						issue.setDescription(issueNode.get("body").asText(null));
						issue.setNumberScope(oneDevProject.getForkRoot());

						Long newNumber;
						Long oldNumber = issueNode.get("number").asLong();
						if (dryRun || (issueManager.find(oneDevProject, oldNumber) == null && !issueNumberMappings.containsValue(oldNumber))) 
							newNumber = oldNumber;
						else
							newNumber = issueManager.getNextNumber(oneDevProject);
						
						issue.setNumber(newNumber);
						issueNumberMappings.put(oldNumber, newNumber);
						
						if (issueNode.get("state").asText().equals("closed"))
							issue.setState(importOption.getClosedIssueState());
						else
							issue.setState(initialIssueState);
						
						if (issueNode.hasNonNull("milestone")) {
							String milestoneName = issueNode.get("milestone").get("title").asText();
							Milestone milestone = milestoneMappings.get(milestoneName);
							if (milestone != null) {
								IssueSchedule schedule = new IssueSchedule();
								schedule.setIssue(issue);
								schedule.setMilestone(milestone);
								issue.getSchedules().add(schedule);
							} else {
								extraIssueInfo.put("Milestone", milestoneName);
								nonExistentMilestones.add(milestoneName);
							}
						}
						
						var userManager = OneDev.getInstance(UserManager.class);
						String login = issueNode.get("user").get("login").asText(null);
						Long userId = getUserId(client, userIds, login, logger);
						if (userId != null) {
							issue.setSubmitter(userManager.load(userId));
						} else {
							issue.setSubmitter(OneDev.getInstance(UserManager.class).getUnknown());
							nonExistentLogins.add(login);
						}
						
						issue.setSubmitDate(ISODateTimeFormat.dateTimeNoMillis()
								.parseDateTime(issueNode.get("created_at").asText())
								.toDate());
						
						LastActivity lastActivity = new LastActivity();
						lastActivity.setDescription("Opened");
						lastActivity.setDate(issue.getSubmitDate());
						lastActivity.setUser(issue.getSubmitter());
						issue.setLastActivity(lastActivity);

						for (JsonNode assigneeNode: issueNode.get("assignees")) {
							IssueField assigneeField = new IssueField();
							assigneeField.setIssue(issue);
							assigneeField.setName(importOption.getAssigneesIssueField());
							assigneeField.setType(InputSpec.USER);
							
							login = assigneeNode.get("login").asText();
							userId = getUserId(client, userIds, login, logger);
							if (userId != null) { 
								assigneeField.setValue(userManager.load(userId).getName());
								issue.getFields().add(assigneeField);
							} else {
								nonExistentLogins.add(login);
							}
						}

						String apiEndpoint = getApiEndpoint("/repos/" + gitHubRepo 
								+ "/issues/" + oldNumber + "/comments");
						for (JsonNode commentNode: list(client, apiEndpoint, logger)) {
							String commentContent = commentNode.get("body").asText(null);
							if (StringUtils.isNotBlank(commentContent)) {
								IssueComment comment = new IssueComment();
								comment.setIssue(issue);
								comment.setContent(commentContent);
								comment.setDate(ISODateTimeFormat.dateTimeNoMillis()
										.parseDateTime(commentNode.get("created_at").asText())
										.toDate());

								login = commentNode.get("user").get("login").asText();
								userId = getUserId(client, userIds, login, logger);
								if (userId != null) {
									comment.setUser(userManager.load(userId));
								} else {
									comment.setUser(OneDev.getInstance(UserManager.class).getUnknown());
									nonExistentLogins.add(login);
								}

								issue.getComments().add(comment);
							}
						}
						
						issue.setCommentCount(issue.getComments().size());
						
						apiEndpoint = getApiEndpoint("/repos/" + gitHubRepo + "/issues/" + oldNumber + "/labels");
						List<String> currentUnmappedLabels = new ArrayList<>();
						for (JsonNode labelNode: list(client, apiEndpoint, logger)) {
							String labelName = labelNode.get("name").asText();
							Pair<FieldSpec, String> mapped = labelMappings.get(labelName);
							if (mapped != null) {
								IssueField tagField = new IssueField();
								tagField.setIssue(issue);
								tagField.setName(mapped.getLeft().getName());
								tagField.setType(InputSpec.ENUMERATION);
								tagField.setValue(mapped.getRight());
								tagField.setOrdinal(mapped.getLeft().getOrdinal(mapped.getRight()));
								issue.getFields().add(tagField);
							} else {
								currentUnmappedLabels.add(labelName);
								unmappedIssueLabels.add(HtmlEscape.escapeHtml5(labelName));
							}
						}

						if (!currentUnmappedLabels.isEmpty()) 
							extraIssueInfo.put("Labels", joinAsMultilineHtml(currentUnmappedLabels));
						
						Set<String> fieldAndValues = new HashSet<>();
						for (IssueField field: issue.getFields()) {
							String fieldAndValue = field.getName() + "::" + field.getValue();
							if (!fieldAndValues.add(fieldAndValue)) {
								String errorMessage = String.format(
										"Duplicate issue field mapping (issue: %s, field: %s)", 
										gitHubRepo + "#" + oldNumber, fieldAndValue);
								throw new ExplicitException(errorMessage);
							}
						}
						
						if (!extraIssueInfo.isEmpty()) {
							StringBuilder builder = new StringBuilder("|");
							for (String key: extraIssueInfo.keySet()) 
								builder.append(key).append("|");
							builder.append("\n|");
							extraIssueInfo.keySet().stream().forEach(it->builder.append("---|"));
							builder.append("\n|");
							for (String value: extraIssueInfo.values())
								builder.append(value).append("|");
							
							if (issue.getDescription() != null)
								issue.setDescription(builder.toString() + "\n\n" + issue.getDescription());
							else
								issue.setDescription(builder.toString());
						}
						issues.add(issue);
						numOfImportedIssues.incrementAndGet();
					}
					logger.log("Imported " + numOfImportedIssues.get() + " issues");
				}
				
			};

			String apiEndpoint = getApiEndpoint("/repos/" + gitHubRepo + "/issues?state=all&direction=asc");
			list(client, apiEndpoint, pageDataConsumer, logger);

			if (!dryRun) {
				ReferenceMigrator migrator = new ReferenceMigrator(Issue.class, issueNumberMappings);
				Dao dao = OneDev.getInstance(Dao.class);
				for (Issue issue: issues) {
					if (issue.getDescription() != null) 
						issue.setDescription(migrator.migratePrefixed(issue.getDescription(), "#"));
					
					dao.persist(issue);
					for (IssueSchedule schedule: issue.getSchedules())
						dao.persist(schedule);
					for (IssueField field: issue.getFields())
						dao.persist(field);
					for (IssueComment comment: issue.getComments()) {
						comment.setContent(migrator.migratePrefixed(comment.getContent(),  "#"));
						dao.persist(comment);
					}
				}
			}
			
			ImportResult result = new ImportResult();
			result.nonExistentLogins.addAll(nonExistentLogins);
			result.nonExistentMilestones.addAll(nonExistentMilestones);
			result.unmappedIssueLabels.addAll(unmappedIssueLabels);
			result.issuesImported = !issues.isEmpty();
			
			if (!dryRun && !issues.isEmpty())
				OneDev.getInstance(ListenerRegistry.class).post(new IssuesImported(oneDevProject, issues));
			
			return result;
		} finally {
			if (!dryRun)
				issueManager.resetNextNumber(oneDevProject.getForkRoot());
			client.close();
		}
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private List<JsonNode> list(Client client, String apiEndpoint, TaskLogger logger) {
		List<JsonNode> result = new ArrayList<>();
		list(client, apiEndpoint, new PageDataConsumer() {

			@Override
			public void consume(List<JsonNode> pageData) {
				result.addAll(pageData);
			}
			
		}, logger);
		return result;
	}
	
	private void list(Client client, String apiEndpoint, PageDataConsumer pageDataConsumer, 
			TaskLogger logger) {
		URI uri;
		try {
			uri = new URIBuilder(apiEndpoint)
					.addParameter("per_page", String.valueOf(PER_PAGE)).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		int page = 1;
		while (true) {
			try {
				URIBuilder builder = new URIBuilder(uri);
				builder.addParameter("page", String.valueOf(page));
				List<JsonNode> pageData = new ArrayList<>();
				for (JsonNode each: get(client, builder.build().toString(), logger)) 
					pageData.add(each);
				pageDataConsumer.consume(pageData);
				if (pageData.size() < PER_PAGE)
					break;
				page++;
			} catch (URISyntaxException|InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private JsonNode get(Client client, String apiEndpoint, TaskLogger logger) {
		WebTarget target = client.target(apiEndpoint);
		Invocation.Builder builder =  target.request();
		while (true) {
			try (Response response = builder.get()) {
				int status = response.getStatus();
				if (status != 200) {
					String errorMessage = response.readEntity(String.class);
					if (StringUtils.isNotBlank(errorMessage)) {
						if (errorMessage.contains("rate limit exceeded")) {
							long resetTime = Long.parseLong(response.getHeaderString("x-ratelimit-reset"))*1000L;
							logger.log("Rate limit exceeded, wait until reset...");
							try {
								Thread.sleep(resetTime + 60*1000L - System.currentTimeMillis());
								continue;
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						} else {
							throw new ExplicitException(String.format("Http request failed (url: %s, status code: %d, error message: %s)", 
									apiEndpoint, status, errorMessage));
						}
					} else {
						throw new ExplicitException(String.format("Http request failed (status: %s)", status));
					}
				} 
				return response.readEntity(JsonNode.class);
			}
		}
	}

	TaskResult importProjects(ImportRepositories repositories, ProjectImportOption option, boolean dryRun, TaskLogger logger) {
		Client client = newClient();
		try {
			Map<String, Optional<Long>> userIds = new HashMap<>();
			ImportResult result = new ImportResult();
			for (var gitHubRepository: repositories.getImportRepositories()) {
				OneDev.getInstance(TransactionManager.class).run(() -> {
					try {
						String oneDevProjectPath;
						if (repositories.getParentOneDevProject() != null)
							oneDevProjectPath = repositories.getParentOneDevProject() + "/" + gitHubRepository;
						else
							oneDevProjectPath = gitHubRepository;

						logger.log("Importing from '" + gitHubRepository + "' to '" + oneDevProjectPath + "'...");

						ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
						Project project = projectManager.setup(oneDevProjectPath);

						if (!project.isNew() && !SecurityUtils.canManage(project)) {
							throw new UnauthorizedException("Import target already exists. " +
									"You need to have project management privilege over it");
						}

						String apiEndpoint = getApiEndpoint("/repos/" + gitHubRepository);
						JsonNode repoNode = get(client, apiEndpoint, logger);

						project.setDescription(repoNode.get("description").asText(null));
						project.setIssueManagement(repoNode.get("has_issues").asBoolean());
						boolean isPrivate = repoNode.get("private").asBoolean();
						if (!isPrivate && option.getPublicRole() != null)
							project.setDefaultRole(option.getPublicRole());

						if (project.isNew() || project.getDefaultBranch() == null) {
							logger.log("Cloning code...");
							URIBuilder builder = new URIBuilder(repoNode.get("clone_url").asText());
							builder.setUserInfo("git", getAccessToken());

							SensitiveMasker.push(text -> StringUtils.replace(text, getAccessToken(), "******"));
							try {
								if (dryRun) {
									new LsRemoteCommand(builder.build().toString()).refs("HEAD").quiet(true).run();
								} else {
									if (project.isNew())
										projectManager.create(project);
									projectManager.clone(project, builder.build().toString());
								}
							} finally {
								SensitiveMasker.pop();
							}
						} else {
							logger.warning("Skipping code clone as the project already has code");
						}

						if (option.getIssueImportOption() != null) {
							logger.log("Importing milestones...");
							apiEndpoint = getApiEndpoint("/repos/" + gitHubRepository + "/milestones?state=all");
							for (JsonNode milestoneNode : list(client, apiEndpoint, logger)) {
								String milestoneName = milestoneNode.get("title").asText();
								Milestone milestone = project.getMilestone(milestoneName);
								if (milestone == null) {
									milestone = new Milestone();
									milestone.setName(milestoneName);
									milestone.setDescription(milestoneNode.get("description").asText(null));
									milestone.setProject(project);
									String dueDateString = milestoneNode.get("due_on").asText(null);
									if (dueDateString != null)
										milestone.setDueDate(ISODateTimeFormat.dateTimeNoMillis().parseDateTime(dueDateString).toDate());
									if (milestoneNode.get("state").asText().equals("closed"))
										milestone.setClosed(true);

									project.getMilestones().add(milestone);

									if (!dryRun)
										OneDev.getInstance(MilestoneManager.class).create(milestone);
								}
							}

							logger.log("Importing issues...");
							ImportResult currentResult = importIssues(gitHubRepository, project, option.getIssueImportOption(),
									userIds, dryRun, logger);
							result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
							result.nonExistentMilestones.addAll(currentResult.nonExistentMilestones);
							result.unmappedIssueLabels.addAll(currentResult.unmappedIssueLabels);
							result.issuesImported = result.issuesImported || currentResult.issuesImported;
						}
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
				});
			}
			return new TaskResult(true, new HtmlMessgae(result.toHtml("Repositories imported successfully")));
		} finally {
			client.close();
		}
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic("git", getAccessToken()));
		try {
			String apiEndpoint = getApiEndpoint("/user");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			try (Response response = builder.get()) {
				if (!response.getMediaType().toString().startsWith("application/json") 
						|| response.getStatus() == 404) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("This does not seem like a GitHub api url")
							.addPropertyNode(PROP_API_URL).addConstraintViolation();
					return false;
				} else if (response.getStatus() == 401) {
					context.disableDefaultConstraintViolation();
					String errorMessage = "Authentication failed";
					context.buildConstraintViolationWithTemplate(errorMessage)
							.addPropertyNode(PROP_ACCESS_TOKEN).addConstraintViolation();
					return false;
				} else {
					String errorMessage = JerseyUtils.checkStatus(apiEndpoint, response);
					if (errorMessage != null) {
						context.disableDefaultConstraintViolation();
						context.buildConstraintViolationWithTemplate(errorMessage)
								.addPropertyNode(PROP_API_URL).addConstraintViolation();
						return false;
					} 
				}
			}
		} catch (Exception e) {
			context.disableDefaultConstraintViolation();
			String errorMessage = "Error connecting api service";
			if (e.getMessage() != null)
				errorMessage += ": " + e.getMessage();
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode(PROP_API_URL).addConstraintViolation();
			return false;
		} finally {
			client.close();
		}
		return true;
	}
	
}
