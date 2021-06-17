package io.onedev.server.plugin.imports.github;

import java.net.URI;
import java.net.URISyntaxException;
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

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.joda.time.format.ISODateTimeFormat;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
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
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.Pair;
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
		if (importSource.isPrepopulateImportOptions()) {
			Client client = newClient(importSource);
			try {
				Set<String> labels = new LinkedHashSet<>();
				for (JsonNode repoNode: list(client, importSource, "/user/repos", logger)) {
					String repoName = repoNode.get("name").asText();
					String ownerName = repoNode.get("owner").get("login").asText();
					GitHubImport aImport = new GitHubImport();
					aImport.setGitHubRepo(ownerName + "/" + repoName);
					aImport.setOneDevProject(ownerName + "-" + repoName);
					importOption.getImports().add(aImport);
					
					for (JsonNode labelNode: list(client, importSource, 
							"/repos/" + aImport.getGitHubRepo() + "/labels", logger)) {
						labels.add(labelNode.get("name").asText());
					}
				}
				for (String label: labels) {
					IssueLabelMapping mapping = new IssueLabelMapping();
					mapping.setGitHubIssueLabel(label);
					importOption.getIssueLabelMappings().add(mapping);
				}
			} finally {
				client.close();
			}
		}
		return importOption;
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
		
		Set<String> nonExistentLogins = new HashSet<>();
		Set<String> unmappedIssueLabels = new HashSet<>();
		
		Map<String, Pair<FieldSpec, String>> labelMappings = new HashMap<>();
		
		for (IssueLabelMapping mapping: importOption.getIssueLabelMappings()) {
			String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
			String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
			FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
			if (fieldSpec == null)
				throw new ExplicitException("No field spec found: " + oneDevFieldName);
			labelMappings.put(mapping.getGitHubIssueLabel(), new Pair<>(fieldSpec, oneDevFieldValue));
		}
		
		Collection<Long> projectIds = new ArrayList<>();
		try {
			Map<String, Optional<User>> users = new HashMap<>();
			boolean importIssues = false;
			for (GitHubImport aImport: importOption.getImports()) {
				logger.log("Cloning code of repository " + aImport.getGitHubRepo() + "...");
				JsonNode repoNode = get(client, importSource, "/repos/" + aImport.getGitHubRepo(), logger);
				Project project = new Project();
				project.setName(aImport.getOneDevProject());
				project.setDescription(repoNode.get("description").asText(null));
				project.setIssueManagementEnabled(repoNode.get("has_issues").asBoolean());
				boolean isPrivate = repoNode.get("private").asBoolean();
				if (!isPrivate && importOption.getPublicRole() != null)
					project.setDefaultRole(importOption.getPublicRole());
				
				URIBuilder builder = new URIBuilder(repoNode.get("clone_url").asText());
				if (isPrivate)
					builder.setUserInfo("git", importSource.getAccessToken());
				
				if (!dryRun) {
					projectManager.clone(project, builder.build().toString());
					projectIds.add(project.getId());
				}

				if (aImport.isImportIssues()) {
					importIssues = true;
					Map<Long, Milestone> milestones = new HashMap<>();
					logger.log("Importing milestones of repository " + aImport.getGitHubRepo() + "...");
					String apiEndpoint = "/repos/" + aImport.getGitHubRepo() + "/milestones?state=all";
					for (JsonNode milestoneNode: list(client, importSource, apiEndpoint, logger)) {
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
					
					logger.log("Importing issues of repository " + aImport.getGitHubRepo() + "...");
					String initialIssueState = getIssueSetting().getInitialStateSpec().getName();
					
					AtomicInteger numOfImportedIssues = new AtomicInteger(0);
					PageDataConsumer pageDataConsumer = new PageDataConsumer() {

						/*
						@Nullable
						private String processAttachments(String issueUUID, Long issueNumber, @Nullable String markdown, 
								Set<String> tooLargeAttachments) {
							if (markdown == null)
								return markdown;

							Map<String, String> attachments = new HashMap<>();
							
							Node node = OneDev.getInstance(MarkdownManager.class).parse(markdown);
							new NodeVisitor(new VisitHandler<?>[] {}) {

								@Override
								public void visit(Node node) {
									super.visit(node);
									if (node instanceof InlineLinkNode) {
										InlineLinkNode link = (InlineLinkNode)node;
										String url = link.getUrl().toString();
										if (url.startsWith(repoUrl + "/files/")) {
											attachments.put(url, link.getText().toString());
										}
									}
								}

							}.visit(node);

							for (Map.Entry<String, String> entry: attachments.entrySet()) {
								String attachmentUrl = entry.getKey();
								String attachmentName = entry.getValue();
								
								WebTarget target = client.target(attachmentUrl);
								Invocation.Builder builder =  target.request();
								try (Response response = builder.get()) {
									String errorMessage = JerseyUtils.checkStatus(response);
									if (errorMessage != null) {
										throw new ExplicitException(String.format(
												"Error downloading attachment (url: %s, error message: %s)", 
												attachmentUrl, errorMessage));
									}
									try (InputStream is = response.readEntity(InputStream.class)) {
										String oneDevAttachmentName = project.saveAttachment(issueUUID, attachmentName, is);
										String oneDevAttachmentUrl = project.getAttachmentUrlPath(issueUUID, oneDevAttachmentName);
										markdown = markdown.replace("(" + attachmentName + ")", "(" + oneDevAttachmentUrl + ")");
									} catch (AttachmentTooLargeException e) {
										tooLargeAttachments.add(aImport.getGitHubRepo() + "#" + issueNumber + ":" + attachmentName);
									} catch (IOException e) {
										throw new RuntimeException(e);
									} 
								}
							}
							
							return markdown;
						}
						*/
						
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
									nonExistentLogins.add(login);
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

								Map<String, String> extraIssueInfo = new LinkedHashMap<>();
								
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
										nonExistentLogins.add(login);
									}
									issue.getFields().add(assigneeField);
								}

								String apiEndpoint = "/repos/" + aImport.getGitHubRepo() 
										+ "/issues/" + issue.getNumber() + "/comments";
								for (JsonNode commentNode: list(client, importSource, apiEndpoint, logger)) {
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
										nonExistentLogins.add(login);
									}

									issue.getComments().add(comment);
								}
								
								issue.setCommentCount(issue.getComments().size());
								
								apiEndpoint = "/repos/" + aImport.getGitHubRepo() 
										+ "/issues/" + issue.getNumber() + "/labels";
								List<String> currentUnmappedLabels = new ArrayList<>();
								for (JsonNode labelNode: list(client, importSource, apiEndpoint, logger)) {
									String labelName = labelNode.get("name").asText();
									Pair<FieldSpec, String> mapped = labelMappings.get(labelName);
									if (mapped != null) {
										IssueField tagField = new IssueField();
										tagField.setIssue(issue);
										tagField.setName(mapped.getFirst().getName());
										tagField.setType(InputSpec.ENUMERATION);
										tagField.setValue(mapped.getSecond());
										tagField.setOrdinal(mapped.getFirst().getOrdinal(mapped.getSecond()));
										issue.getFields().add(tagField);
									} else {
										currentUnmappedLabels.add(HtmlEscape.escapeHtml5(labelName));
										unmappedIssueLabels.add(HtmlEscape.escapeHtml5(labelName));
									}
								}
								
								if (!currentUnmappedLabels.isEmpty()) 
									extraIssueInfo.put("Labels", joinAsMultilineHtml(currentUnmappedLabels));
								
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
								
								if (!dryRun) {
									OneDev.getInstance(IssueManager.class).save(issue);
									for (IssueField field: issue.getFields())
										OneDev.getInstance(Dao.class).persist(field);
									for (IssueComment comment: issue.getComments())
										OneDev.getInstance(Dao.class).persist(comment);
								}
								
								if (numOfImportedIssues.incrementAndGet() % 25 == 0)
									logger.log("Imported " + numOfImportedIssues.get() + " issues");
							}
						}
						
					};
					apiEndpoint = "/repos/" + aImport.getGitHubRepo() + "/issues?state=all";
					list(client, importSource, apiEndpoint, pageDataConsumer, logger);
				}
			}
			
			StringBuilder feedback = new StringBuilder("Repositories imported successfully");
			
			boolean hasNotes = !unmappedIssueLabels.isEmpty() || !nonExistentLogins.isEmpty() || importIssues;

			if (hasNotes)
				feedback.append("<br><br><b>NOTE:</b><ul>");
			
			if (!unmappedIssueLabels.isEmpty()) { 
				feedback.append("<li> GitHub issue labels not mapped to OneDev custom field (mentioned as extra info in issue description): " 
						+ HtmlEscape.escapeHtml5(unmappedIssueLabels.toString()));
			}
			if (!nonExistentLogins.isEmpty()) {
				feedback.append("<li> GitHub logins without public email or public email can not be mapped to OneDev account: " 
						+ HtmlEscape.escapeHtml5(nonExistentLogins.toString()));
			}
			if (importIssues)
				feedback.append("<li> Attachments in issues and comments are not imported due to GitHub limitation");
			
			if (hasNotes)
				feedback.append("</ul>");
			
			return feedback.toString();
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
			uri = new URIBuilder(importSource.getApiEndpoint(apiPath))
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
		return get(client, importSource.getApiEndpoint(apiPath), logger);
	}
	
	private JsonNode get(Client client, String apiEndpoint, SimpleLogger logger) {
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
							throw new ExplicitException(String.format("Http request failed (status code: %d, error message: %s)", 
									status, errorMessage));
						}
					} else {
						throw new ExplicitException(String.format("Http request failed (status: %s)", status));
					}
				} 
				return response.readEntity(JsonNode.class);
			}
		}
	}
	
	private Client newClient(GitHubImportSource importSource) {
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		client.register(HttpAuthenticationFeature.basic("git", importSource.getAccessToken()));
		return client;
	}

	private static interface PageDataConsumer {
		
		void consume(List<JsonNode> pageData) throws InterruptedException;
		
	}
	
}