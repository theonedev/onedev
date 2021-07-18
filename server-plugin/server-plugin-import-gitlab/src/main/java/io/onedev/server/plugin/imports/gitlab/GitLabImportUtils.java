package io.onedev.server.plugin.imports.gitlab;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.joda.time.format.ISODateTimeFormat;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
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
import io.onedev.server.util.AttachmentTooLargeException;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ReferenceMigrator;
import io.onedev.server.util.SimpleLogger;

public class GitLabImportUtils {
	
	static final String NAME = "GitLab";

	static final int PER_PAGE = 100;
	
	private static final Pattern PATTERN_ATTACHMENT = Pattern.compile("\\[(.+?)\\]\\s*\\((/uploads/.+?)\\)");
	
	static GitLabIssueImportOption buildImportOption(GitLabProjectImportSource importSource, 
			@Nullable String gitLabProject, SimpleLogger logger) {
		GitLabIssueImportOption importOption = new GitLabIssueImportOption();
		Client client = newClient(importSource);
		try {
			List<String> gitLabProjects = new ArrayList<>();
			if (gitLabProject == null) {
				String apiEndpoint = importSource.getApiEndpoint("/projects?membership=true");
				for (JsonNode projectNode: list(client, apiEndpoint, logger)) 
					gitLabProjects.add(projectNode.get("path_with_namespace").asText());
			} else {
				gitLabProjects.add(gitLabProject);
			}

			Set<String> labels = new LinkedHashSet<>();
			for (String each: gitLabProjects) {
				String apiEndpoint = importSource.getApiEndpoint("/projects/" + each.replace("/", "%2F") + "/labels"); 
				for (JsonNode labelNode: list(client, apiEndpoint, logger)) 
					labels.add(labelNode.get("name").asText());
			}
			
			for (String label: labels) {
				IssueLabelMapping mapping = new IssueLabelMapping();
				mapping.setGitLabIssueLabel(label);
				importOption.getIssueLabelMappings().add(mapping);
			}
		} finally {
			client.close();
		}
		return importOption;
	}
	
	@Nullable
	static User getUser(Client client, GitLabProjectImportSource importSource, Map<String, Optional<User>> users, 
			String userId, SimpleLogger logger) {
		Optional<User> userOpt = users.get(userId);
		if (userOpt == null) {
			String apiEndpoint = importSource.getApiEndpoint("/users/" + userId);
			JsonNode userNode = get(client, apiEndpoint, logger);
			String email = null;
			if (userNode.hasNonNull("email"))
				email = userNode.get("email").asText(null);
			if (email == null && userNode.hasNonNull("public_email"))
				email = userNode.get("public_email").asText(null);
			if (email != null)
				userOpt = Optional.ofNullable(OneDev.getInstance(UserManager.class).findByEmail(email));
			else
				userOpt = Optional.empty();
			users.put(userId, userOpt);
		}
		return userOpt.orElse(null);
	}
	
	static GitLabImportResult importIssues(GitLabProjectImportSource importSource, String gitLabProject, Project oneDevProject,
			boolean useExistingIssueNumbers, GitLabIssueImportOption importOption, Map<String, Optional<User>> users, 
			boolean dryRun, SimpleLogger logger) {
		Client client = newClient(importSource);
		try {
			Set<String> nonExistentMilestones = new HashSet<>();
			Set<String> nonExistentLogins = new HashSet<>();
			Set<String> unmappedIssueLabels = new HashSet<>();
			Set<String> tooLargeAttachments = new LinkedHashSet<>();
			Set<String> errorAttachments = new HashSet<>();
			
			Map<String, Pair<FieldSpec, String>> labelMappings = new HashMap<>();
			Map<String, Milestone> milestoneMappings = new HashMap<>();
			
			for (IssueLabelMapping mapping: importOption.getIssueLabelMappings()) {
				String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
				String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				labelMappings.put(mapping.getGitLabIssueLabel(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			
			for (Milestone milestone: oneDevProject.getMilestones())
				milestoneMappings.put(milestone.getName(), milestone);
			
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
									String oneDevAttachmentName = oneDevProject.saveAttachment(issueUUID, attachmentName, is);
									String oneDevAttachmentUrl = oneDevProject.getAttachmentUrlPath(issueUUID, oneDevAttachmentName);
							    	matcher.appendReplacement(buffer, "[" + matcher.group(1) + "](" + oneDevAttachmentUrl + ")");  
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
						
						issue.setNumberScope(oneDevProject);

						Long oldNumber = issueNode.get("iid").asLong();
						Long newNumber;
						if (dryRun || useExistingIssueNumbers)
							newNumber = oldNumber;
						else
							newNumber = OneDev.getInstance(IssueManager.class).getNextNumber(oneDevProject);
						issue.setNumber(newNumber);
						issueNumberMappings.put(oldNumber, newNumber);
						
						String issueFQN = gitLabProject + "#" + oldNumber;
						
						if (issueNode.get("state").asText().equals("closed"))
							issue.setState(importOption.getClosedIssueState());
						else
							issue.setState(initialIssueState);
						
						if (issueNode.hasNonNull("milestone")) {
							String milestoneName = issueNode.get("milestone").get("title").asText();
							Milestone milestone = milestoneMappings.get(milestoneName);
							if (milestone != null) {
								issue.setMilestone(milestone);
							} else {
								extraIssueInfo.put("Milestone", milestoneName);
								nonExistentMilestones.add(milestoneName);
							}
						}
						
						JsonNode authorNode = issueNode.get("author");
						User user = getUser(client, importSource, users, authorNode.get("id").asText(), logger);
						if (user != null) {
							issue.setSubmitter(user);
						} else {
							issue.setSubmitterName(authorNode.get("name").asText());
							nonExistentLogins.add(authorNode.get("username").asText());
						}
						
						issue.setSubmitDate(ISODateTimeFormat.dateTime()
								.parseDateTime(issueNode.get("created_at").asText())
								.toDate());
						
						LastUpdate lastUpdate = new LastUpdate();
						lastUpdate.setActivity("Opened");
						lastUpdate.setDate(issue.getSubmitDate());
						lastUpdate.setUser(issue.getSubmitter());
						lastUpdate.setUserName(issue.getSubmitterName());
						issue.setLastUpdate(lastUpdate);

						List<JsonNode> assigneeNodes = new ArrayList<>();
						if (issueNode.hasNonNull("assignees")) {
							for (JsonNode assigneeNode: issueNode.get("assignees")) 
								assigneeNodes.add(assigneeNode);
						} else {
							assigneeNodes.add(issueNode.get("assignee"));
						}
						
						for (JsonNode assigneeNode: assigneeNodes) {
							IssueField assigneeField = new IssueField();
							assigneeField.setIssue(issue);
							assigneeField.setName(importOption.getAssigneesIssueField());
							assigneeField.setType(InputSpec.USER);
							
							user = getUser(client, importSource, users, assigneeNode.get("id").asText(), logger);
							if (user != null) { 
								assigneeField.setValue(user.getName());
							} else {
								String assigneeLogin = assigneeNode.get("username").asText();
								assigneeField.setValue(assigneeLogin);
								nonExistentLogins.add(assigneeLogin);
							}
							issue.getFields().add(assigneeField);
						}

						if (importOption.getDueDateIssueField() != null) {
							String dueDate = issueNode.get("due_date").asText(null);
							if (dueDate != null) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(importOption.getDueDateIssueField());
								issueField.setType(InputSpec.DATE);
								issueField.setValue(dueDate);
								issue.getFields().add(issueField);
							}
						}
						
						JsonNode timeStatsNode = issueNode.get("time_stats");
						if (importOption.getEstimatedTimeIssueField() != null) {
							int value = timeStatsNode.get("time_estimate").asInt();
							if (value != 0) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(importOption.getEstimatedTimeIssueField());
								issueField.setType(InputSpec.WORKING_PERIOD);
								issueField.setValue(DateUtils.formatWorkingPeriod(value/60));
								issue.getFields().add(issueField);
							}
						}
						if (importOption.getSpentTimeIssueField() != null) {
							int value = timeStatsNode.get("total_time_spent").asInt();
							if (value != 0) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(importOption.getSpentTimeIssueField());
								issueField.setType(InputSpec.WORKING_PERIOD);
								issueField.setValue(DateUtils.formatWorkingPeriod(value/60));
								issue.getFields().add(issueField);
							}
						}
						
						List<String> currentUnmappedLabels = new ArrayList<>();
						for (JsonNode labelNode: issueNode.get("labels")) {
							String labelName = labelNode.asText();
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
						
						String webUrl = issueNode.get("web_url").asText();
						String attachmentRootUrl = StringUtils.substringBeforeLast(webUrl, "/-");
						if (!dryRun && issue.getDescription() != null) {
							issue.setDescription(processAttachments(issue.getUUID(), issueFQN, 
									issue.getDescription(), attachmentRootUrl, tooLargeAttachments));
						}
						
						String apiEndpoint = importSource.getApiEndpoint("/projects/" + gitLabProject.replace("/", "%2F") 
								+ "/issues/" + oldNumber + "/notes?sort=asc");
						for (JsonNode noteNode: list(client, apiEndpoint, logger)) {
							if (!noteNode.get("system").asBoolean()) {
								String commentContent = noteNode.get("body").asText(null); 
								if (commentContent != null) {
									if (!dryRun) {
										commentContent = processAttachments(issue.getUUID(), issueFQN, 
												commentContent, attachmentRootUrl, tooLargeAttachments);
									}
									
									IssueComment comment = new IssueComment();
									comment.setIssue(issue);
									comment.setContent(commentContent);
									comment.setDate(ISODateTimeFormat.dateTime()
											.parseDateTime(noteNode.get("created_at").asText())
											.toDate());
									
									authorNode = noteNode.get("author");
									user = getUser(client, importSource, users, authorNode.get("id").asText(), logger);
									if (user != null) {
										comment.setUser(user);
									} else {
										comment.setUserName(authorNode.get("name").asText());
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
										"Duplicate issue field mapping (issue: #%d, field: %s)", 
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

			String apiEndpoint = importSource.getApiEndpoint("/projects/" + gitLabProject.replace("/", "%2F") + "/issues?sort=asc");
			list(client, apiEndpoint, pageDataConsumer, logger);

			if (!dryRun) {
				ReferenceMigrator migrator = new ReferenceMigrator(Issue.class, issueNumberMappings);
				for (Issue issue: issues) {
					if (issue.getDescription() != null) 
						issue.setDescription(migrator.migratePrefixed(issue.getDescription(), "#"));
					
					OneDev.getInstance(IssueManager.class).save(issue);
					for (IssueField field: issue.getFields())
						OneDev.getInstance(Dao.class).persist(field);
					for (IssueComment comment: issue.getComments()) {
						comment.setContent(migrator.migratePrefixed(comment.getContent(),  "#"));
						OneDev.getInstance(Dao.class).persist(comment);
					}
				}
			}
			
			GitLabImportResult result = new GitLabImportResult();
			result.nonExistentLogins.addAll(nonExistentLogins);
			result.nonExistentMilestones.addAll(nonExistentMilestones);
			result.unmappedIssueLabels.addAll(unmappedIssueLabels);
			result.tooLargeAttachments.addAll(tooLargeAttachments);
			result.errorAttachments.addAll(errorAttachments);
			
			return result;
		} finally {
			client.close();
		}
	}
	
	static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	static List<JsonNode> list(Client client, String apiEndpoint, SimpleLogger logger) {
		List<JsonNode> result = new ArrayList<>();
		list(client, apiEndpoint, new PageDataConsumer() {

			@Override
			public void consume(List<JsonNode> pageData) {
				result.addAll(pageData);
			}
			
		}, logger);
		return result;
	}
	
	static void list(Client client, String apiEndpoint, PageDataConsumer pageDataConsumer, 
			SimpleLogger logger) {
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
	
	static JsonNode get(Client client, String apiEndpoint, SimpleLogger logger) {
		WebTarget target = client.target(apiEndpoint);
		Invocation.Builder builder =  target.request();
		while (true) {
			try (Response response = builder.get()) {
				int status = response.getStatus();
				if (status != 200) {
					String errorMessage = response.readEntity(String.class);
					if (StringUtils.isNotBlank(errorMessage)) {
						throw new ExplicitException(String.format("Http request failed (url: %s, status code: %d, error message: %s)", 
								apiEndpoint, status, errorMessage));
					} else {
						throw new ExplicitException(String.format("Http request failed (status: %s)", status));
					}
				} 
				return response.readEntity(JsonNode.class);
			}
		}
	}
	
	static Client newClient(GitLabProjectImportSource importSource) {
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		client.register(OAuth2ClientSupport.feature(importSource.getAccessToken()));
		return client;
	}

	static interface PageDataConsumer {
		
		void consume(List<JsonNode> pageData) throws InterruptedException;
		
	}
	
}