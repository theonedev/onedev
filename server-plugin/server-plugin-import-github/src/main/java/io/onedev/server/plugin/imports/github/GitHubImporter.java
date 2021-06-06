package io.onedev.server.plugin.imports.github;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.joda.time.format.ISODateTimeFormat;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.page.project.imports.ProjectImporter;

public class GitHubImporter extends ProjectImporter<GitHubImportSource, GitHubImportOption> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "GitHub";

	private static final int PER_PAGE = 100;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public GitHubImportOption getImportOption(GitHubImportSource importSource, SimpleLogger logger) {
		GitHubImportOption importOption = new GitHubImportOption();
		Client client = newClient(importSource);
		try {
			for (JsonNode repoNode: list(client, importSource, "/user/repos", logger)) {
				String repoName = repoNode.get("name").asText();
				String ownerName = repoNode.get("owner").get("login").asText();
				GitHubImport aImport = new GitHubImport();
				aImport.setGithubRepo(ownerName + "/" + repoName);
				aImport.setProjectName(ownerName + "-" + repoName);
				importOption.getImports().add(aImport);
			}
			return importOption;
		} finally {
			client.close();
		}
	}
	
	@Nullable
	private User getUser(Client client, GitHubImportSource importSource, 
			Map<String, Optional<User>> users, String login, SimpleLogger logger) {
		Optional<User> userOpt = users.get(login);
		if (userOpt == null) {
			String email = get(client, importSource, "/users/" + login, logger).get("email").asText(null);
			if (email != null) 
				userOpt = Optional.ofNullable(OneDev.getInstance(UserManager.class).findByEmail(email));
			else 
				userOpt = Optional.empty();
			users.put(login, userOpt);
		}
		return userOpt.orElse(null);
	}
	
	@Override
	public String doImport(GitHubImportSource importSource, GitHubImportOption importOption, 
			boolean dryRun, SimpleLogger logger) {
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		StorageManager storageManager = OneDev.getInstance(StorageManager.class);
		Client client = newClient(importSource);
		
		Set<String> loginsWithoutAccount = new HashSet<>();
		Set<String> unmappedIssueLabels = new HashSet<>();
		
		Collection<Long> projectIds = new ArrayList<>();
		try {
			Map<String, Optional<User>> users = new HashMap<>();
			for (GitHubImport aImport: importOption.getImports()) {
				logger.log("Cloning code of repository " + aImport.getGithubRepo() + "...");
				JsonNode repoNode = get(client, importSource, "/repos/" + aImport.getGithubRepo(), logger);
				Project project = new Project();
				project.setName(aImport.getProjectName());
				project.setDescription(repoNode.get("description").asText(null));
				project.setIssueManagementEnabled(repoNode.get("has_issues").asBoolean());
				boolean isPrivate = repoNode.get("private").asBoolean();
				if (!isPrivate)
					project.setDefaultRole(importOption.getPublicRole());
				URIBuilder builder = new URIBuilder(repoNode.get("clone_url").asText());
				if (isPrivate)
					builder.setUserInfo("git", importSource.getAccessToken());
				
				if (!dryRun) {
					projectManager.clone(project, builder.build().toString());
					projectIds.add(project.getId());
				}

				if (aImport.isImportIssues()) {
					Map<Long, Milestone> milestones = new HashMap<>();
					logger.log("Importing milestones of repository " + aImport.getGithubRepo() + "...");
					String apiUrl = "/repos/" + aImport.getGithubRepo() + "/milestones?state=all";
					for (JsonNode milestoneNode: list(client, importSource, apiUrl, logger)) {
						Milestone milestone = new Milestone();
						milestone.setName(milestoneNode.get("title").asText());
						milestone.setDescription(milestoneNode.get("description").asText(null));
						milestone.setProject(project);
						String dueDateString = milestoneNode.get("due_on").asText(null);
						if (dueDateString != null) 
							milestone.setDueDate(ISODateTimeFormat.dateTimeNoMillis().parseDateTime(dueDateString).toDate());
						if (milestoneNode.get("state").asText().equals("closed"))
							milestone.setClosed(true);
						
						if (!dryRun)
							OneDev.getInstance(MilestoneManager.class).save(milestone);
						milestones.put(milestoneNode.get("number").asLong(), milestone);
					}
					
					logger.log("Importing issues of repository " + aImport.getGithubRepo() + "...");
					String initialIssueState = getIssueSetting().getInitialStateSpec().getName();
					
					AtomicInteger numOfImportedIssues = new AtomicInteger(0);
					PageDataConsumer pageHandler = new PageDataConsumer() {

						@Override
						public void consume(List<JsonNode> page) throws InterruptedException {
							for (JsonNode issueNode: page) {
								if (Thread.interrupted())
									throw new InterruptedException();
								Issue issue = new Issue();
								issue.setProject(project);
								issue.setTitle(issueNode.get("title").asText());
								issue.setDescription(issueNode.get("body").asText(null));
								issue.setNumberScope(project);
								issue.setNumber(issueNode.get("number").asLong());
								if (issueNode.get("state").asText().equals("closed"))
									issue.setState(importOption.getClosedIssueState());
								else
									issue.setState(initialIssueState);
								if (issueNode.hasNonNull("milestone")) {
									long milestoneNumber = issueNode.get("milestone").get("number").asLong();
									Milestone milestone = milestones.get(milestoneNumber);
									if (milestone != null)
										issue.setMilestone(milestone);
								}
								String login = issueNode.get("user").get("login").asText(null);
								User user = getUser(client, importSource, users, login, logger);
								if (user != null) {
									issue.setSubmitter(user);
								} else {
									issue.setSubmitterName(login);
									loginsWithoutAccount.add(login);
								}
								
								issue.setSubmitDate(ISODateTimeFormat.dateTimeNoMillis()
										.parseDateTime(issueNode.get("created_at").asText())
										.toDate());
								
								LastUpdate lastUpdate = new LastUpdate();
								lastUpdate.setActivity("Opened");
								lastUpdate.setDate(issue.getSubmitDate());
								lastUpdate.setUser(issue.getSubmitter());
								lastUpdate.setUserName(issue.getSubmitterName());
								issue.setLastUpdate(lastUpdate);

								if (!dryRun)
									OneDev.getInstance(IssueManager.class).save(issue);
								
								for (JsonNode assigneeNode: issueNode.get("assignees")) {
									IssueField assigneeField = new IssueField();
									assigneeField.setIssue(issue);
									assigneeField.setName(importOption.getAssigneesIssueField());
									assigneeField.setType(InputSpec.USER);
									
									login = assigneeNode.get("login").asText();
									user = getUser(client, importSource, users, login, logger);
									if (user != null) { 
										assigneeField.setValue(user.getName());
									} else {
										assigneeField.setValue(login);
										loginsWithoutAccount.add(login);
									}
									if (!dryRun)
										getIssueFieldManager().save(assigneeField);
								}

								String apiUrl = "/repos/" + aImport.getGithubRepo() 
										+ "/issues/" + issue.getNumber() + "/comments";
								for (JsonNode commentNode: list(client, importSource, apiUrl, logger)) {
									IssueComment comment = new IssueComment();
									comment.setIssue(issue);
									comment.setContent(commentNode.get("body").asText(null));
									comment.setDate(ISODateTimeFormat.dateTimeNoMillis()
											.parseDateTime(commentNode.get("created_at").asText())
											.toDate());
									
									login = commentNode.get("user").get("login").asText();
									user = getUser(client, importSource, users, login, logger);
									if (user != null) {
										comment.setUser(user);
									} else {
										comment.setUserName(login);
										loginsWithoutAccount.add(login);
									}
									
									if (!dryRun)
										OneDev.getInstance(IssueCommentManager.class).save(comment);
								}
								
								apiUrl = "/repos/" + aImport.getGithubRepo() 
										+ "/issues/" + issue.getNumber() + "/labels";
								for (JsonNode labelNode: list(client, importSource, apiUrl, logger)) {
									String labelName = labelNode.get("name").asText();
									IssueField labelField = null;
									for (LabelMapping mapping: importOption.getLabelMappings()) {
										if (mapping.getIssueLabel().equals(labelName)) {
											String fieldName = StringUtils.substringBefore(mapping.getIssueField(), ":").trim();
											FieldSpec fieldSpec = getIssueSetting().getFieldSpecMap(null).get(fieldName);
											if (fieldSpec != null) {
												labelField = new IssueField();
												labelField.setIssue(issue);
												String fieldValue = StringUtils.substringAfter(mapping.getIssueField(), ":").trim();
												labelField.setName(fieldName);
												labelField.setType(InputSpec.ENUMERATION);
												labelField.setValue(fieldValue);
												labelField.setOrdinal(fieldSpec.getOrdinal(fieldValue));
												if (!dryRun)
													getIssueFieldManager().save(labelField);
											}
											break;
										}
									}
									if (labelField == null)
										unmappedIssueLabels.add(labelName);
								}
								if (numOfImportedIssues.incrementAndGet() % 25 == 0)
									logger.log("Imported " + numOfImportedIssues.get() + " issues");
							}
						}
						
					};
					apiUrl = "/repos/" + aImport.getGithubRepo() + "/issues?state=all";
					list(client, importSource, apiUrl, pageHandler, logger);
				}
			}
			List<String> result = new ArrayList<>();
			result.add("All repositories imported successfully");
			if (!loginsWithoutAccount.isEmpty()) {
				result.add("<b>NOTE:</b> GitHub logins without public email or public email can not be mapped to OneDev account: " 
						+ HtmlEscape.escapeHtml5(loginsWithoutAccount.toString()));
			}
			if (!unmappedIssueLabels.isEmpty()) { 
				result.add("<b>NOTE:</b> Issue labels not mapped to custom field: " 
						+ HtmlEscape.escapeHtml5(unmappedIssueLabels.toString()));
			}
			return StringUtils.join(result, "<br>");
		} catch (Exception e) {
			for (Long projectId: projectIds)
				storageManager.deleteProjectDir(projectId);
			throw new RuntimeException(e);
		} finally {
			client.close();
		}
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private IssueFieldManager getIssueFieldManager() {
		return OneDev.getInstance(IssueFieldManager.class);
	}

	private List<JsonNode> list(Client client, GitHubImportSource importSource, 
			String apiPath, SimpleLogger logger) {
		List<JsonNode> result = new ArrayList<>();
		list(client, importSource, apiPath, new PageDataConsumer() {

			@Override
			public void consume(List<JsonNode> pageData) {
				result.addAll(pageData);
			}
			
		}, logger);
		return result;
	}
	
	private void list(Client client, GitHubImportSource importSource, String apiPath, 
			PageDataConsumer pageDataConsumer, SimpleLogger logger) {
		URI uri;
		try {
			uri = new URIBuilder(importSource.getApiUrl(apiPath))
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
	
	private JsonNode get(Client client, GitHubImportSource importSource, String apiPath, SimpleLogger logger) {
		return get(client, importSource.getApiUrl(apiPath), logger);
	}
	
	private JsonNode get(Client client, String apiUrl, SimpleLogger logger) {
		WebTarget target = client.target(apiUrl);
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
							throw new ExplicitException(String.format("Http request failed (status code: %d, error message: %s)", 
									status, errorMessage));
						}
					} else {
						throw new RuntimeException("Http request failed with status " + status 
								+ ", check server log for detaiils");
					}
				} 
				return response.readEntity(JsonNode.class);
			}
		}
	}
	
	private Client newClient(GitHubImportSource importSource) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic("git", importSource.getAccessToken()));
		return client;
	}

	private static interface PageDataConsumer {
		
		void consume(List<JsonNode> page) throws InterruptedException;
		
	}
}