package io.onedev.server.plugin.imports.gitlab;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.authz.UnauthorizedException;
import org.glassfish.jersey.client.ClientProperties;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.bootstrap.SecretMasker;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.attachment.AttachmentService;
import io.onedev.server.attachment.AttachmentTooLargeException;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.BaseAuthorizationService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IterationService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.issue.IssuesImported;
import io.onedev.server.git.command.LsRemoteCommand;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.JerseyUtils.PageDataConsumer;
import io.onedev.server.util.Pair;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.taskbutton.TaskResult.HtmlMessgae;

@Editable
@ClassValidating
public class ImportServer implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ImportServer.class);
	
	private static final int PER_PAGE = 50;
	
	private static final Pattern PATTERN_ATTACHMENT = Pattern.compile("\\[(.+?)\\]\\s*\\((/uploads/.+?)\\)");
	
	private static final String PROP_API_URL = "apiUrl";
	
	private static final String PROP_ACCESS_TOKEN = "accessToken";
	
	private String apiUrl;
	
	private String accessToken;

	private transient Map<String, String> groups;
	
	@Editable(order=10, name="GitLab API URL", description="Specify GitLab API url, for instance <tt>https://gitlab.example.com/api/v4</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Editable(order=100, name="GitLab Personal Access Token", description="GitLab personal access token should be generated with "
			+ "scope <b>read_api</b>, <b>read_user</b> and <b>read_repository</b>. Note that only groups/projects owned by " +
			"user of specified access token will be listed")
	@Password
	@NotEmpty
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	Map<String, String> listGroups() {
		if (groups == null) {
			groups = new HashMap<>();
			Client client = newClient();
			try {
				String apiEndpoint = getApiEndpoint("/groups?owned=true&top_level_only=true");
				for (JsonNode groupNode: list(client, apiEndpoint, new TaskLogger() {

					@Override
					public void log(String message, String sessionId) {
						logger.info(message);
					}
					
				})) {
					groups.put(groupNode.get("id").asText(), groupNode.get("full_path").asText());
				}	
			} catch (Exception e) {
				logger.error("Error listing groups", e);
			} finally {
				client.close();
			}
			groups = CollectionUtils.sortByValue(groups);
		}
		return groups;
	}
	
	List<String> listProjects(@Nullable String groupId, boolean includeForks) {
		List<String> projects = new ArrayList<>();
		Client client = newClient();
		try {
			String apiEndpoint;
			if (groupId != null) 
				apiEndpoint = getApiEndpoint("/groups/" + groupId + "/projects?include_subgroups=true");
			else 
				apiEndpoint = getApiEndpoint("/projects?owned=true");
			Collection<JsonNode> projectNodes = list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			});
			for (JsonNode projectNode: projectNodes) {
				if ((groupId != null || projectNode.get("namespace").get("kind").asText().equals("user")) 
						&& (includeForks || projectNode.get("forked_from_project") == null)) {
					projects.add(projectNode.get("path_with_namespace").asText());
				}
			}
		} finally {
			client.close();
		}
		
		Collections.sort(projects);
		return projects;
	}

	private String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}

	private Client newClient() {
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		
		// Pass token with PRIVATE-TOKEN header to be compatible with old GitLab version
		//client.register(OAuth2ClientSupport.feature(getAccessToken()));
		client.register(new AccessTokenFilter(getAccessToken()));
		return client;
	}
	
	IssueImportOption buildIssueImportOption(Collection<String> gitLabProjects) {
		IssueImportOption option = new IssueImportOption();
		Client client = newClient();
		try {
			TaskLogger taskLogger = new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			};
			Set<String> labels = new LinkedHashSet<>();
			for (String each: gitLabProjects) {
				String apiEndpoint = getApiEndpoint("/projects/" + each.replace("/", "%2F") + "/labels"); 
				for (JsonNode labelNode: list(client, apiEndpoint, taskLogger)) 
					labels.add(labelNode.get("name").asText());
			}
			
			for (String label: labels) {
				IssueLabelMapping mapping = new IssueLabelMapping();
				mapping.setGitLabIssueLabel(label);
				option.getIssueLabelMappings().add(mapping);
			}
		} finally {
			client.close();
		}
		return option;
	}
	
	@Nullable
	private Long getUserId(Client client, Map<String, Optional<Long>> userIds,
						   String gitlabUserId, TaskLogger logger) {
		Optional<Long> userIdOpt = userIds.get(gitlabUserId);
		if (userIdOpt == null) {
			String apiEndpoint = getApiEndpoint("/users/" + gitlabUserId);
			JsonNode userNode = JerseyUtils.get(client, apiEndpoint, logger);
			String email = null;
			if (userNode.hasNonNull("email"))
				email = userNode.get("email").asText(null);
			if (email == null && userNode.hasNonNull("public_email"))
				email = userNode.get("public_email").asText(null);
			if (email != null)
				userIdOpt = Optional.ofNullable(User.idOf(OneDev.getInstance(UserService.class).findByVerifiedEmailAddress(email)));
			else
				userIdOpt = Optional.empty();
			userIds.put(gitlabUserId, userIdOpt);
		}
		return userIdOpt.orElse(null);
	}
	
	private List<Iteration> getIterations(String groupId, TaskLogger logger) {
		Client client = newClient();
		try {
			List<Iteration> iterations = new ArrayList<>();
			String apiEndpoint = getApiEndpoint("/groups/" + groupId + "/milestones");
			for (JsonNode milestoneNode: list(client, apiEndpoint, logger)) 
				iterations.add(getIteration(milestoneNode));
			apiEndpoint = getApiEndpoint("/groups/" + groupId);
			JsonNode groupNode = JerseyUtils.get(client, apiEndpoint, logger);
			JsonNode parentIdNode = groupNode.get("parent_id");
			if (parentIdNode != null && parentIdNode.asText(null) != null) 
				iterations.addAll(getIterations(parentIdNode.asText(), logger));
			return iterations;
		} finally {
			client.close();
		}
	}
	
	private Iteration getIteration(JsonNode milestoneNode) {
		Iteration iteration = new Iteration();
		iteration.setName(milestoneNode.get("title").asText());
		iteration.setDescription(milestoneNode.get("description").asText(null));
		String dueDateString = milestoneNode.get("due_date").asText(null);
		if (dueDateString != null) 
			iteration.setDueDay(DateUtils.toLocalDate(ISODateTimeFormat.date().parseDateTime(dueDateString).toDate(), ZoneId.systemDefault()).toEpochDay());
		if (milestoneNode.get("state").asText().equals("closed"))
			iteration.setClosed(true);
		return iteration;
	}
	
	TaskResult importProjects(ImportProjects projects, ProjectImportOption option, boolean dryRun, TaskLogger logger) {
		Client client = newClient();
		try {
			Map<String, Optional<Long>> userIds = new HashMap<>();
			ImportResult result = new ImportResult();
			for (var gitLabProject: projects.getImportProjects()) {
				OneDev.getInstance(TransactionService.class).run(() -> {
					try {
						String oneDevProjectPath;
						if (projects.getParentOneDevProject() != null)
							oneDevProjectPath = projects.getParentOneDevProject() + "/" + gitLabProject;
						else
							oneDevProjectPath = gitLabProject;

						logger.log("Importing from '" + gitLabProject + "' to '" + oneDevProjectPath + "'...");

						ProjectService projectService = OneDev.getInstance(ProjectService.class);
						Project project = projectService.setup(SecurityUtils.getSubject(), oneDevProjectPath);

						if (!project.isNew() && !SecurityUtils.canManageProject(project)) {
							throw new UnauthorizedException("Import target already exists. " +
									"You need to have project management privilege over it");
						}

						String apiEndpoint = getApiEndpoint("/projects/" + gitLabProject.replace("/", "%2F"));
						JsonNode projectNode = JerseyUtils.get(client, apiEndpoint, logger);

						project.setDescription(projectNode.get("description").asText(null));
						project.setIssueManagement(projectNode.get("issues_enabled").asBoolean());

						if (project.isNew() || project.getDefaultBranch() == null) {
							logger.log("Cloning code...");
							URIBuilder builder = new URIBuilder(projectNode.get("http_url_to_repo").asText());
							builder.setUserInfo("git", getAccessToken());

							SecretMasker.push(new SecretMasker() {

								@Override
								public String mask(String text) {
									return StringUtils.replace(text, getAccessToken(), "******");
								}

							});
							try {
								if (dryRun) {
									new LsRemoteCommand(builder.build().toString()).refs("HEAD").quiet(true).run();
								} else {
									if (project.isNew()) {
										projectService.create(SecurityUtils.getUser(), project);
										OneDev.getInstance(AuditService.class).audit(project, "created project", null, VersionedXmlDoc.fromBean(project).toXML());
									}
									projectService.clone(project, builder.build().toString());
								}
							} finally {
								SecretMasker.pop();
							}
						} else {
							logger.warning("Skipping code clone as the project already has code");
						}

						String visibility = projectNode.get("visibility").asText();
						if (!visibility.equals("private") && !option.getPublicRoles().isEmpty())
							OneDev.getInstance(BaseAuthorizationService.class).syncRoles(project, option.getPublicRoles());

						if (option.getIssueImportOption() != null) {
							List<Iteration> iterations = new ArrayList<>();
							logger.log("Importing milestones...");
							apiEndpoint = getApiEndpoint("/projects/"
									+ gitLabProject.replace("/", "%2F") + "/milestones");
							for (JsonNode milestoneNode : list(client, apiEndpoint, logger))
								iterations.add(getIteration(milestoneNode));
							JsonNode namespaceNode = projectNode.get("namespace");
							if (namespaceNode.get("kind").asText().equals("group")) {
								String groupId = namespaceNode.get("id").asText();
								iterations.addAll(getIterations(groupId, logger));
							}

							for (Iteration iteration : iterations) {
								if (project.getIteration(iteration.getName()) == null) {
									iteration.setProject(project);
									project.getIterations().add(iteration);
									if (!dryRun)
										OneDev.getInstance(IterationService.class).createOrUpdate(iteration);
								}
							}

							logger.log("Importing issues...");
							ImportResult currentResult = importIssues(gitLabProject, project, option.getIssueImportOption(),
									userIds, dryRun, logger);
							result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
							result.nonExistentIterations.addAll(currentResult.nonExistentIterations);
							result.unmappedIssueLabels.addAll(currentResult.unmappedIssueLabels);
							result.tooLargeAttachments.addAll(currentResult.tooLargeAttachments);
							result.errorAttachments.addAll(currentResult.errorAttachments);
						}
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
				});
			}
			return new TaskResult(true, new HtmlMessgae(result.toHtml("Projects imported successfully")));
		} finally {
			client.close();
		}
	}
	
	ImportResult importIssues(String gitLabProject, Project oneDevProject, IssueImportOption option, 
			Map<String, Optional<Long>> userIds, boolean dryRun, TaskLogger logger) {
		IssueService issueService = OneDev.getInstance(IssueService.class);
		Client client = newClient();
		try {
			Set<String> nonExistentIterations = new HashSet<>();
			Set<String> nonExistentLogins = new HashSet<>();
			Set<String> unmappedIssueLabels = new HashSet<>();
			Set<String> tooLargeAttachments = new LinkedHashSet<>();
			Set<String> errorAttachments = new HashSet<>();
			
			Map<String, Pair<FieldSpec, String>> labelMappings = new HashMap<>();
			Map<String, Iteration> iterationMappings = new HashMap<>();
			
			for (IssueLabelMapping mapping: option.getIssueLabelMappings()) {
				String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
				String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				labelMappings.put(mapping.getGitLabIssueLabel(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			
			for (Iteration iteration: oneDevProject.getIterations())
				iterationMappings.put(iteration.getName(), iteration);
			
			String initialIssueState = getIssueSetting().getInitialStateSpec().getName();
				
			List<Issue> issues = new ArrayList<>();
			Map<Long, Long> issueNumberMappings = new HashMap<>();
			
			AtomicInteger numOfImportedIssues = new AtomicInteger(0);
			PageDataConsumer pageDataConsumer = new PageDataConsumer() {

				@Nullable
				private String processAttachments(String issueUUID, String issueFQN, String markdown, 
						String attachmentRootUrl, Set<String> tooLargeAttachments) {
				    StringBuffer buffer = new StringBuffer();  
				    Matcher matcher = PATTERN_ATTACHMENT.matcher(markdown);  
				    while (matcher.find()) {  
				    	String attachmentUrl = attachmentRootUrl + matcher.group(2);
				    	String attachmentName = StringUtils.substringAfterLast(attachmentUrl, "/");
						WebTarget target = client.target(attachmentUrl);
						Invocation.Builder builder =  target.request();
						try (Response response = builder.get()) {
							String errorMessage = JerseyUtils.checkStatus(attachmentUrl, response);
							if (errorMessage != null) { 
								logger.error("Error downloading attachment: " + errorMessage); 
								errorAttachments.add(attachmentUrl);
							} else {
								try (InputStream is = response.readEntity(InputStream.class)) {
									AttachmentService attachmentService = OneDev.getInstance(AttachmentService.class);
									String oneDevAttachmentName = attachmentService.saveAttachment(
											oneDevProject.getId(), issueUUID, attachmentName, is);
									String oneDevAttachmentUrl = oneDevProject.getAttachmentUrlPath(issueUUID, oneDevAttachmentName);
							    	matcher.appendReplacement(buffer, 
							    			Matcher.quoteReplacement("[" + matcher.group(1) + "](" + oneDevAttachmentUrl + ")"));  
								} catch (AttachmentTooLargeException ex) {
									tooLargeAttachments.add(issueFQN + ":" + matcher.group(2));
								} catch (IOException e) {
									logger.error("Error downloading attachment", e); 
									errorAttachments.add(attachmentUrl);
								} 
							}
						}
				    }  
				    matcher.appendTail(buffer);  
				    
				    return buffer.toString();
				}
				
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

						Map<String, String> extraIssueInfo = new LinkedHashMap<>();
						
						Issue issue = new Issue();
						issue.setProject(oneDevProject);
						issue.setTitle(issueNode.get("title").asText());
						issue.setDescription(issueNode.get("description").asText(null));
						issue.setConfidential(issueNode.get("confidential").asBoolean());
						
						issue.setNumberScope(oneDevProject.getForkRoot());

						Long newNumber;
						Long oldNumber = issueNode.get("iid").asLong();
						if (dryRun || (issueService.find(oneDevProject, oldNumber) == null && !issueNumberMappings.containsValue(oldNumber))) 
							newNumber = oldNumber;
						else
							newNumber = issueService.getNextNumber(oneDevProject);
						
						issue.setNumber(newNumber);
						issueNumberMappings.put(oldNumber, newNumber);
						
						String issueFQN = gitLabProject + "#" + oldNumber;
						
						if (issueNode.get("state").asText().equals("closed"))
							issue.setState(option.getClosedIssueState());
						else
							issue.setState(initialIssueState);
						
						if (issueNode.hasNonNull("milestone")) {
							String milestoneName = issueNode.get("milestone").get("title").asText();
							Iteration iteration = iterationMappings.get(milestoneName);
							if (iteration != null) {
								IssueSchedule schedule = new IssueSchedule();
								schedule.setIssue(issue);
								schedule.setIteration(iteration);
								issue.getSchedules().add(schedule);
							} else {
								extraIssueInfo.put("Milestone", milestoneName);
								nonExistentIterations.add(milestoneName);
							}
						}
						
						var userService = OneDev.getInstance(UserService.class);
						JsonNode authorNode = issueNode.get("author");
						Long userId = getUserId(client, userIds, authorNode.get("id").asText(), logger);
						if (userId != null) {
							issue.setSubmitter(userService.load(userId));
						} else {
							issue.setSubmitter(OneDev.getInstance(UserService.class).getUnknown());
							nonExistentLogins.add(authorNode.get("username").asText());
						}
						
						issue.setSubmitDate(ISODateTimeFormat.dateTime()
								.parseDateTime(issueNode.get("created_at").asText())
								.toDate());
						
						LastActivity lastActivity = new LastActivity();
						lastActivity.setDescription("Opened");
						lastActivity.setDate(issue.getSubmitDate());
						lastActivity.setUser(issue.getSubmitter());
						issue.setLastActivity(lastActivity);

						List<JsonNode> assigneeNodes = new ArrayList<>();
						if (issueNode.hasNonNull("assignees")) {
							for (JsonNode assigneeNode: issueNode.get("assignees")) 
								assigneeNodes.add(assigneeNode);
						} else if (issueNode.hasNonNull("assignee")) {
							assigneeNodes.add(issueNode.get("assignee"));
						}
						
						for (JsonNode assigneeNode: assigneeNodes) {
							IssueField assigneeField = new IssueField();
							assigneeField.setIssue(issue);
							assigneeField.setName(option.getAssigneesIssueField());
							assigneeField.setType(InputSpec.USER);
							
							userId = getUserId(client, userIds, assigneeNode.get("id").asText(), logger);
							if (userId != null) { 
								assigneeField.setValue(userService.load(userId).getName());
								issue.getFields().add(assigneeField);
							} else {
								nonExistentLogins.add(assigneeNode.get("username").asText());
							}
						}

						if (option.getDueDateIssueField() != null) {
							String dueDate = issueNode.get("due_date").asText(null);
							if (dueDate != null) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(option.getDueDateIssueField());
								issueField.setType(InputSpec.DATE);
								issueField.setValue(dueDate);
								issue.getFields().add(issueField);
							}
						}
						
						JsonNode timeStatsNode = issueNode.get("time_stats");
						if (option.getEstimatedTimeIssueField() != null) {
							int value = timeStatsNode.get("time_estimate").asInt();
							if (value != 0) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(option.getEstimatedTimeIssueField());
								issueField.setType(InputSpec.INTEGER);
								issueField.setValue(String.valueOf(value));
								issue.getFields().add(issueField);
							}
						}
						if (option.getSpentTimeIssueField() != null) {
							int value = timeStatsNode.get("total_time_spent").asInt();
							if (value != 0) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(option.getSpentTimeIssueField());
								issueField.setType(InputSpec.INTEGER);
								issueField.setValue(String.valueOf(value));
								issue.getFields().add(issueField);
							}
						}
						
						List<String> currentUnmappedLabels = new ArrayList<>();
						for (JsonNode labelNode: issueNode.get("labels")) {
							String labelName = labelNode.asText();
							Pair<FieldSpec, String> mapped = labelMappings.get(labelName);
							if (mapped != null) {
								IssueField labelField = new IssueField();
								labelField.setIssue(issue);
								labelField.setName(mapped.getLeft().getName());
								labelField.setType(InputSpec.ENUMERATION);
								labelField.setValue(mapped.getRight());
								labelField.setOrdinal(mapped.getLeft().getOrdinal(mapped.getRight()));
								issue.getFields().add(labelField);
							} else {
								currentUnmappedLabels.add(labelName);
								unmappedIssueLabels.add(HtmlEscape.escapeHtml5(labelName));
							}
						}

						if (!currentUnmappedLabels.isEmpty()) 
							extraIssueInfo.put("Labels", joinAsMultilineHtml(currentUnmappedLabels));
						
						String webUrl = issueNode.get("web_url").asText();
						String attachmentRootUrl = StringUtils.substringBeforeLast(webUrl, "/-");
						if (!dryRun && issue.getDescription() != null) {
							issue.setDescription(processAttachments(issue.getUUID(), issueFQN, 
									issue.getDescription(), attachmentRootUrl, tooLargeAttachments));
						}
						
						String apiEndpoint = getApiEndpoint("/projects/" + gitLabProject.replace("/", "%2F") 
								+ "/issues/" + oldNumber + "/links");
						Map<String, List<String>> links = new LinkedHashMap<>();
						for (JsonNode linkNode: list(client, apiEndpoint, logger)) {
							String linkIssueNumber = linkNode.get("iid").asText();
							if (linkNode.get("references").get("full").asText().equals(gitLabProject + "#" + linkIssueNumber)) {
								String linkType = linkNode.get("link_type").asText();
								List<String> linksOfType = links.get(linkType);
								if (linksOfType == null) {
									linksOfType = new ArrayList<>();
									links.put(linkType, linksOfType);
								}
								linksOfType.add("#" + linkIssueNumber);
							}
						}
						for (Map.Entry<String, List<String>> entry: links.entrySet()) 
							extraIssueInfo.put(entry.getKey(), joinAsMultilineHtml(entry.getValue()));
						
						apiEndpoint = getApiEndpoint("/projects/" + gitLabProject.replace("/", "%2F") 
								+ "/issues/" + oldNumber + "/notes?sort=asc");
						for (JsonNode noteNode: list(client, apiEndpoint, logger)) {
							if (!noteNode.get("system").asBoolean()) {
								String commentContent = noteNode.get("body").asText(null); 
								if (StringUtils.isNotBlank(commentContent)) {
									if (!dryRun) {
										commentContent = processAttachments(issue.getUUID(), issueFQN, 
												commentContent, attachmentRootUrl, tooLargeAttachments);
										if (StringUtils.isBlank(commentContent))
											continue;
									}
									
									IssueComment comment = new IssueComment();
									comment.setIssue(issue);
									comment.setContent(commentContent);
									comment.setDate(ISODateTimeFormat.dateTime()
											.parseDateTime(noteNode.get("created_at").asText())
											.toDate());
									
									authorNode = noteNode.get("author");
									userId = getUserId(client, userIds, authorNode.get("id").asText(), logger);
									if (userId != null) {
										comment.setUser(userService.load(userId));
									} else {
										comment.setUser(OneDev.getInstance(UserService.class).getUnknown());
										nonExistentLogins.add(authorNode.get("username").asText());
									}
									issue.getComments().add(comment);
								}
							}
						}
						
						issue.setCommentCount(issue.getComments().size());

						Set<String> fieldAndValues = new HashSet<>();
						for (IssueField field: issue.getFields()) {
							String fieldAndValue = field.getName() + "::" + field.getValue();
							if (!fieldAndValues.add(fieldAndValue)) {
								String errorMessage = String.format(
										"Duplicate issue field mapping (issue: %s, field: %s)", 
										issueFQN, fieldAndValue);
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
					}
					logger.log("Imported " + numOfImportedIssues.addAndGet(pageData.size()) + " issues");
				}
				
			};

			String apiEndpoint = getApiEndpoint("/projects/" + gitLabProject.replace("/", "%2F") + "/issues?sort=asc");
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
			result.nonExistentIterations.addAll(nonExistentIterations);
			result.unmappedIssueLabels.addAll(unmappedIssueLabels);
			result.tooLargeAttachments.addAll(tooLargeAttachments);
			result.errorAttachments.addAll(errorAttachments);
			
			if (!dryRun && !issues.isEmpty())
				OneDev.getInstance(ListenerRegistry.class).post(new IssuesImported(oneDevProject, issues));
			
			return result;
		} finally {
			if (!dryRun)
				issueService.resetNextNumber(oneDevProject);
			client.close();
		}
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingService.class).getIssueSetting();
	}
	
	List<JsonNode> list(Client client, String apiEndpoint, TaskLogger logger) {
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
				for (JsonNode each: JerseyUtils.get(client, builder.build().toString(), logger)) 
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

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		client.register(new AccessTokenFilter(accessToken));
		try {
			String apiEndpoint = getApiEndpoint("/user");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			try (Response response = builder.get()) {
				if (!response.getMediaType().toString().startsWith("application/json") 
						|| response.getStatus() == 404) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("This does not seem like a GitLab api url")
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
