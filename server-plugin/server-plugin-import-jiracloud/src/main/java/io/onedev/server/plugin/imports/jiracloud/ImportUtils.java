package io.onedev.server.plugin.imports.jiracloud;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.JerseyUtils.PageDataConsumer;
import io.onedev.server.util.Pair;

public class ImportUtils {

	static final String NAME = "JIRA Cloud";
	
	static final int PER_PAGE = 50;
	
	static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Nullable
	static User getUser(Map<String, Optional<User>> users, JsonNode userNode, TaskLogger logger) {
		String accountId = userNode.get("accountId").asText();
		Optional<User> userOpt = users.get(accountId);
		if (userOpt == null) {
			String displayName = null;
			if (userNode.hasNonNull("displayName"))
				displayName = userNode.get("displayName").asText(null);
			if (displayName != null)
				userOpt = Optional.ofNullable(OneDev.getInstance(UserManager.class).findByFullName(displayName));
			else
				userOpt = Optional.empty();
			users.put(accountId, userOpt);
		}
		return userOpt.orElse(null);
	}
	
	private static String getMarkdown(JsonNode contentNode, boolean escape) {
		StringBuilder markdown = new StringBuilder();
		for (JsonNode itemNode: contentNode) {
			String type = itemNode.get("type").asText();
			switch (type) {
			case "text":
				String text = itemNode.get("text").asText();
				if (escape)
					text = HtmlEscape.escapeHtml5(text);
				
				JsonNode marksNode = itemNode.get("marks");
				if (marksNode != null) {
					for (JsonNode markNode: marksNode) {
						String textType = markNode.get("type").asText(null);
						switch (textType) {
						case "strong":
							text = "**" + text.replace("*", "\\*") + "**";
							break;
						case "em":
							text = "_" + text.replace("_", "\\_") + "_";
							break;
						case "code":
							text = "`" + text + "`";
							break;
						case "textColor":
							text = String.format("<span style='color:%s'>%s</span>", 
									markNode.get("attrs").get("color").asText(), text);
							break;
						case "link":
							text = String.format("[%s](%s)", text, markNode.get("attrs").get("href").asText());
							break;
						}
					}
				}
				markdown.append(text);
				break;
			case "mention":
				markdown.append("@" + itemNode.get("attrs").get("text").asText());
				break;
			case "heading":
				markdown.append(StringUtils.repeat('#', itemNode.get("attrs").get("level").asInt())).append(" ");
				markdown.append(getMarkdown(itemNode.get("content"), true)).append("\n\n");
				break;	
			case "paragraph":
				markdown.append(getMarkdown(itemNode.get("content"), true)).append("\n\n");
				break;
			case "bulletList":
				for (JsonNode listItemNode: itemNode.get("content")) 
					markdown.append("- ").append(getMarkdown(listItemNode.get("content"), true));
				break;
			case "orderedList":
				for (JsonNode listItemNode: itemNode.get("content")) 
					markdown.append("1. ").append(getMarkdown(listItemNode.get("content"), true));
				break;
			case "inlineCard":
				markdown.append(" " + StringUtils.substringAfterLast(itemNode.get("attrs").get("url").asText(), "/") + " ");
				break;
			case "emoji":
				markdown.append(" " + itemNode.get("attrs").get("shortName").asText() + " ");
				break;
			case "codeBlock":
				markdown.append("```\n" + getMarkdown(itemNode.get("content"), false) + "\n```\n\n");
				break;
			case "panel":
				String panelType = itemNode.get("attrs").get("panelType").asText();
				switch (panelType) {
				case "info":
				case "note":
					markdown.append(":information_source:\n");
					break;
				case "success":
					markdown.append(":white_check_mark:\n");
					break;
				case "warning":
					markdown.append(":warning:\n");
					break;
				case "error":
					markdown.append(":no_entry:\n");
					break;
				}
				markdown.append(getMarkdown(itemNode.get("content"), true));
				break;
			case "blockquote":
				List<String> lines = Splitter.on('\n').splitToList(getMarkdown(itemNode.get("content"), true));
				for (String line: lines)
					markdown.append("> ").append(line).append("\n");
				break;
			case "rule":
				markdown.append("---\n");
				break;
			case "date":
				markdown.append(DateUtils.formatDate(new Date(itemNode.get("attrs").get("timestamp").asLong())));
				break;
			case "status":
				JsonNode attrsNode = itemNode.get("attrs");
				text = HtmlEscape.escapeHtml5(attrsNode.get("text").asText());
				String color = attrsNode.get("color").asText();
				switch (color) {
				case "yellow":
					markdown.append(String.format("<span style='background:#FFF0B3; padding:2px 4px; border-radius:3px; font-weight:bold; color:#172B4D;'>%s</span>", text));
					break;
				case "neutral":
					markdown.append(String.format("<span style='background:#DFE1E6; padding:2px 4px; border-radius:3px; font-weight:bold; color:#42526E;'>%s</span>", text));
					break;
				case "purple":
					markdown.append(String.format("<span style='background:#EAE6FF; padding:2px 4px; border-radius:3px; font-weight:bold; color:#574BA3;'>%s</span>", text));
					break;
				case "blue":
					markdown.append(String.format("<span style='background:#DEEBFF; padding:2px 4px; border-radius:3px; font-weight:bold; color:#0747A6;'>%s</span>", text));
					break;
				case "red":
					markdown.append(String.format("<span style='background:#FFEBE6; padding:2px 4px; border-radius:3px; font-weight:bold; color:#BF2600;'>%s</span>", text));
					break;
				case "green":
					markdown.append(String.format("<span style='background:#E3FCEF; padding:2px 4px; border-radius:3px; font-weight:bold; color:#006644;'>%s</span>", text));
					break;
				default:
					markdown.append(text);
				}
				break;
			case "table":
				List<List<String>> tableData = new ArrayList<>();
				for (JsonNode rowNode: itemNode.get("content")) {
					List<String> columns = new ArrayList<>();
					for (JsonNode columnNode: rowNode.get("content"))
						columns.add(getMarkdown(columnNode.get("content"), true).replace("\n", "<br>"));
					tableData.add(columns);
				}
				if (!tableData.isEmpty()) {
					if (!tableData.get(0).isEmpty()) {
						markdown.append("|");
						for (int i=0; i<tableData.get(0).size(); i++)
							markdown.append("-----|");
						markdown.append("\n");
						for (List<String> row: tableData) {
							markdown.append("|");
							for (String column: row) 
								markdown.append(" ").append(column).append(" |");
							markdown.append("\n");
						}
						markdown.append("\n");
					}
				}
				break;
			}
		}
		return markdown.toString();
	}
	
	static ImportResult importIssues(ImportServer server, JsonNode jiraProject, Project oneDevProject, 
			boolean retainIssueNumbers, ImportOption importOption, 
			Map<String, Optional<User>> users, boolean dryRun, TaskLogger logger) {
		Client client = server.newClient();
		try {
			Set<String> unmappedIssueStatuses = new HashSet<>();
			Set<String> unmappedIssueTypes = new HashSet<>();
			Set<String> unmappedIssuePriorities = new HashSet<>();
			Set<String> nonExistentLogins = new HashSet<>();
			Set<String> tooLargeAttachments = new LinkedHashSet<>();
			Set<String> errorAttachments = new HashSet<>();
			
			Map<String, Pair<FieldSpec, String>> typeMappings = new HashMap<>();
			
			for (IssueTypeMapping mapping: importOption.getIssueTypeMappings()) {
				String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
				String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				typeMappings.put(mapping.getJiraIssueType(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			
			Map<String, Pair<FieldSpec, String>> priorityMappings = new HashMap<>();
			
			for (IssuePriorityMapping mapping: importOption.getIssuePriorityMappings()) {
				String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
				String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				priorityMappings.put(mapping.getJiraIssuePriority(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			
			String initialIssueState = getIssueSetting().getInitialStateSpec().getName();
				
			List<Issue> issues = new ArrayList<>();
			
			Map<Long, Long> issueNumberMappings = new HashMap<>();
			
			Map<String, String> statusMappings = new HashMap<>();
			for (IssueStatusMapping mapping: importOption.getIssueStatusMappings())
				statusMappings.put(mapping.getJiraIssueStatus(), mapping.getOneDevIssueState());
			
			Map<String, String> untranslatedStatusNames = new HashMap<>();
			String apiEndpoint = server.getApiEndpoint("/status");
			for (JsonNode statusNode: list(client, apiEndpoint, logger)) {
				String untranslated = statusNode.get("untranslatedName").asText();
				untranslatedStatusNames.put(statusNode.get("id").asText(), untranslated);
			}
			
			Map<String, String> untranslatedTypeNames = new HashMap<>();
			apiEndpoint = server.getApiEndpoint("/issuetype/project?projectId=" + jiraProject.get("id").asText());
			for (JsonNode typeNode: list(client, apiEndpoint, logger)) 
				untranslatedTypeNames.put(typeNode.get("id").asText(), typeNode.get("untranslatedName").asText());
			
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

						String issueKey = issueNode.get("key").asText();
						String apiEndpoint = server.getApiEndpoint("/issue/" + issueKey);
						issueNode = JerseyUtils.get(client, apiEndpoint, logger);
						
						Map<String, String> extraIssueInfo = new LinkedHashMap<>();
						
						Issue issue = new Issue();
						issue.setProject(oneDevProject);
						
						issue.setNumberScope(oneDevProject.getForkRoot());

						Long oldNumber = Long.valueOf(StringUtils.substringAfterLast(issueNode.get("key").asText(), "-"));
						Long newNumber;
						if (dryRun || retainIssueNumbers)
							newNumber = oldNumber;
						else
							newNumber = OneDev.getInstance(IssueManager.class).getNextNumber(oneDevProject);
						issue.setNumber(newNumber);
						issueNumberMappings.put(oldNumber, newNumber);
						
						JsonNode fieldsNode = issueNode.get("fields");
						
						issue.setTitle(fieldsNode.get("summary").asText());

						String description = "";
						JsonNode descriptionNode = fieldsNode.get("description");
						if (descriptionNode != null && descriptionNode.hasNonNull("content")) {
							description = getMarkdown(descriptionNode.get("content"), true);
							if (StringUtils.isNotBlank("description"))
								description = "#### Description\n\n" + description;
						}
						
						JsonNode environmentNode = fieldsNode.get("environment");
						if (environmentNode != null && environmentNode.hasNonNull("content")) {
							String environment = getMarkdown(environmentNode.get("content"), true);
							if (StringUtils.isNotBlank(environment)) {
								if (StringUtils.isNotBlank(description))
									description += "\n";
								description += "#### Environment\n\n" + environment;
							}
						}
						
						if (StringUtils.isNotBlank(description))
							issue.setDescription(description);
						
						String statusId = fieldsNode.get("status").get("id").asText();
						String untranslatedStatusName = untranslatedStatusNames.get(statusId);
						if (untranslatedStatusName != null) {
							String statusName = statusMappings.get(untranslatedStatusName);
							if (statusName != null) {
								issue.setState(statusName);
							} else {
								unmappedIssueStatuses.add(untranslatedStatusName);
								issue.setState(initialIssueState);
							}
						} else {
							throw new ExplicitException("Can not find JIRA status of id: " + statusId);
						}
						
						String typeId = fieldsNode.get("issuetype").get("id").asText();
						String untranslatedTypeName = untranslatedTypeNames.get(typeId);
						if (untranslatedTypeName != null) {
							Pair<FieldSpec, String> mapped = typeMappings.get(untranslatedTypeName);
							if (mapped != null) {
								IssueField typeField = new IssueField();
								typeField.setIssue(issue);
								typeField.setName(mapped.getFirst().getName());
								typeField.setType(InputSpec.ENUMERATION);
								typeField.setValue(mapped.getSecond());
								typeField.setOrdinal(mapped.getFirst().getOrdinal(mapped.getSecond()));
								issue.getFields().add(typeField);
							} else {
								extraIssueInfo.put("Type", HtmlEscape.escapeHtml5(untranslatedTypeName));
								unmappedIssueTypes.add(untranslatedTypeName);
							}
						} else {
							String errorMessage = String.format("Can not find JIRA issue type by id (project: %s, issue type id: %s)", 
									jiraProject.get("name").asText(), typeId);
							throw new ExplicitException(errorMessage);
						}

						String priorityName = fieldsNode.get("priority").get("name").asText();
						Pair<FieldSpec, String> mapped = priorityMappings.get(priorityName);
						if (mapped != null) {
							IssueField priorityField = new IssueField();
							priorityField.setIssue(issue);
							priorityField.setName(mapped.getFirst().getName());
							priorityField.setType(InputSpec.ENUMERATION);
							priorityField.setValue(mapped.getSecond());
							priorityField.setOrdinal(mapped.getFirst().getOrdinal(mapped.getSecond()));
							issue.getFields().add(priorityField);
						} else {
							extraIssueInfo.put("Priority", HtmlEscape.escapeHtml5(priorityName));
							unmappedIssuePriorities.add(priorityName);
						}
						
						List<String> components = new ArrayList<>();
						for (JsonNode componentNode: fieldsNode.get("components")) 
							components.add(componentNode.get("name").asText());
						if (!components.isEmpty()) 
							extraIssueInfo.put("Components", joinAsMultilineHtml(components));

						List<String> affectedVersions = new ArrayList<>();
						for (JsonNode versionNode: fieldsNode.get("versions")) 
							affectedVersions.add(versionNode.get("name").asText());
						if (!affectedVersions.isEmpty())
							extraIssueInfo.put("Affected Versions", joinAsMultilineHtml(affectedVersions));

						List<String> fixVersions = new ArrayList<>();
						for (JsonNode versionNode: fieldsNode.get("fixVersions")) 
							fixVersions.add(versionNode.get("name").asText());
						if (!fixVersions.isEmpty())
							extraIssueInfo.put("Fix Versions", joinAsMultilineHtml(fixVersions));
						
						if (fieldsNode.hasNonNull("labels")) {
							List<String> labels = new ArrayList<>();
							for (JsonNode labelNode: fieldsNode.get("labels")) 
								labels.add(labelNode.asText());
							if (!labels.isEmpty())
								extraIssueInfo.put("Labels", joinAsMultilineHtml(labels));
						}
						
						JsonNode authorNode = fieldsNode.get("reporter");
						if (authorNode == null)
							authorNode = fieldsNode.get("creator");
						if (authorNode != null) {
							User user = getUser(users, authorNode, logger);
							if (user != null) {
								issue.setSubmitter(user);
							} else {
								issue.setSubmitter(OneDev.getInstance(UserManager.class).getUnknown());
								nonExistentLogins.add(authorNode.get("displayName").asText());
							}
						} else {
							issue.setSubmitter(OneDev.getInstance(UserManager.class).getUnknown());
						}
						
						if (fieldsNode.hasNonNull("assignee")) {
							JsonNode assigneeNode = fieldsNode.get("assignee");
							IssueField assigneeField = new IssueField();
							assigneeField.setIssue(issue);
							assigneeField.setName(importOption.getAssigneeIssueField());
							assigneeField.setType(InputSpec.USER);
							
							User user = getUser(users, assigneeNode, logger);
							if (user != null) { 
								assigneeField.setValue(user.getName());
								issue.getFields().add(assigneeField);
							} else {
								nonExistentLogins.add(assigneeNode.get("displayName").asText());
							}
						}
						
						issue.setSubmitDate(ISODateTimeFormat.dateTime()
								.parseDateTime(fieldsNode.get("created").asText())
								.toDate());
						
						LastUpdate lastUpdate = new LastUpdate();
						lastUpdate.setActivity("Opened");
						lastUpdate.setDate(issue.getSubmitDate());
						lastUpdate.setUser(issue.getSubmitter());
						issue.setLastUpdate(lastUpdate);

						if (importOption.getDueDateIssueField() != null) {
							String dueDate = fieldsNode.get("duedate").asText(null);
							if (dueDate != null) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(importOption.getDueDateIssueField());
								issueField.setType(InputSpec.DATE);
								issueField.setValue(dueDate);
								issue.getFields().add(issueField);
							}
						}
						
						if (importOption.getTimeEstimateIssueField() != null) {
							String timeEstimate = fieldsNode.get("timeestimate").asText(null);
							if (timeEstimate != null) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(importOption.getTimeEstimateIssueField());
								issueField.setType(InputSpec.WORKING_PERIOD);
								issueField.setValue(DateUtils.formatWorkingPeriod(Integer.valueOf(timeEstimate)/60));
								issue.getFields().add(issueField);
							}
						}
						if (importOption.getTimeSpentIssueField() != null) {
							String timeSpent = fieldsNode.get("timespent").asText(null);
							if (timeSpent != null) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(importOption.getTimeSpentIssueField());
								issueField.setType(InputSpec.WORKING_PERIOD);
								issueField.setValue(DateUtils.formatWorkingPeriod(Integer.valueOf(timeSpent)/60));
								issue.getFields().add(issueField);
							}
						}

						if (fieldsNode.hasNonNull("subtasks")) {
							List<String> subtasks = new ArrayList<>();
							for (JsonNode subtaskNode: fieldsNode.get("subtasks")) 
								subtasks.add(subtaskNode.get("key").asText());
							if (!subtasks.isEmpty())
								extraIssueInfo.put("Sub Tasks", StringUtils.join(subtasks, "<br>"));
						}

						if (fieldsNode.hasNonNull("parent")) 
							extraIssueInfo.put("Parent Task", fieldsNode.get("parent").get("key").asText());
						
						if (fieldsNode.hasNonNull("issuelinks")) {
							Map<String, List<String>> issueLinks = new LinkedHashMap<>();
							for (JsonNode issueLinkNode: fieldsNode.get("issuelinks")) {
								if (issueLinkNode.get("inwardIssue") != null) {
									String type = issueLinkNode.get("type").get("inward").asText();
									List<String> linkedIssues = issueLinks.get(type);
									if (linkedIssues == null) {
										linkedIssues = new ArrayList<>();
										issueLinks.put(type, linkedIssues);
									}
									linkedIssues.add(issueLinkNode.get("inwardIssue").get("key").asText());
								} else {
									String type = issueLinkNode.get("type").get("outward").asText();
									List<String> linkedIssues = issueLinks.get(type);
									if (linkedIssues == null) {
										linkedIssues = new ArrayList<>();
										issueLinks.put(type, linkedIssues);
									}
									linkedIssues.add(issueLinkNode.get("outwardIssue").get("key").asText());
								}
							}
							for (Map.Entry<String, List<String>> entry: issueLinks.entrySet()) 
								extraIssueInfo.put(entry.getKey(), StringUtils.join(entry.getValue(), "<br>"));
						}
						
						if (fieldsNode.hasNonNull("comment") && fieldsNode.get("comment").hasNonNull("comments")) {
							for (JsonNode commentNode: fieldsNode.get("comment").get("comments")) {
								String commentContent = getMarkdown(commentNode.get("body").get("content"), true); 
								if (StringUtils.isNotBlank(commentContent)) {
									IssueComment comment = new IssueComment();
									comment.setIssue(issue);
									comment.setContent(commentContent);
									
									comment.setDate(ISODateTimeFormat.dateTime()
											.parseDateTime(commentNode.get("created").asText())
											.toDate());
									
									authorNode = commentNode.get("author");
									if (authorNode != null) {
										User user = getUser(users, authorNode, logger);
										if (user != null) {
											comment.setUser(user);
										} else {
											comment.setUser(OneDev.getInstance(UserManager.class).getUnknown());
											nonExistentLogins.add(authorNode.get("displayName").asText());
										}
									} else {
										comment.setUser(OneDev.getInstance(UserManager.class).getUnknown());
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
										issueKey, fieldAndValue);
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
						
						if (!dryRun) { 
							List<String> attachments = new ArrayList<>();
							
							if (fieldsNode.hasNonNull("attachment")) {
								long maxUploadFileSize = OneDev.getInstance(SettingManager.class)
										.getPerformanceSetting().getMaxUploadFileSize()*1L*1024*1024; 
								for (JsonNode attachmentNode: fieldsNode.get("attachment")) {
									String attachmentName = attachmentNode.get("filename").asText();
									int attachmentSize = attachmentNode.get("size").asInt();
									if (attachmentSize >  maxUploadFileSize) {
										tooLargeAttachments.add(issueKey + ":" + attachmentName);
									} else {
										String endpoint = attachmentNode.get("content").asText();
										WebTarget target = client.target(endpoint);
										Invocation.Builder builder =  target.request();
										try (Response response = builder.get()) {
											String errorMessage = JerseyUtils.checkStatus(endpoint, response);
											if (errorMessage != null) { 
												throw new ExplicitException(String.format(
														"Error downloading attachment (url: %s, error message: %s)", 
														endpoint, errorMessage));
											}
											try (InputStream is = response.readEntity(InputStream.class)) {
												String oneDevAttachmentName = oneDevProject.saveAttachment(issue.getUUID(), attachmentName, is);
												String oneDevAttachmentUrl = oneDevProject.getAttachmentUrlPath(issue.getUUID(), oneDevAttachmentName);
												attachments.add("[" + oneDevAttachmentName + "](" + oneDevAttachmentUrl + ")");
											} catch (IOException e) {
												throw new RuntimeException(e);
											} 
										}
									}
								}
							}
							
							if (!attachments.isEmpty()) {
								description = issue.getDescription();
								if (StringUtils.isNotBlank(description)) 
									description += "\n";
								description += "#### Attachments\n" + Joiner.on(" &nbsp;&nbsp;&nbsp;&bull;&nbsp;&nbsp;&nbsp; ").join(attachments);
								issue.setDescription(description);
							}
						}

						issues.add(issue);
					}
					logger.log("Imported " + numOfImportedIssues.addAndGet(pageData.size()) + " issues");
				}
				
			};

			String jiraProjectName = jiraProject.get("name").asText();
			String jql = String.format("project = \"%s\" order by created DESC", jiraProjectName);
			jql = URLEncoder.encode(jql, StandardCharsets.UTF_8.name());
			apiEndpoint = server.getApiEndpoint("/search?jql=" + jql);
			list(client, apiEndpoint, pageDataConsumer, logger);

			if (!dryRun) {
				ReferenceMigrator migrator = new ReferenceMigrator(Issue.class, issueNumberMappings);
				String jiraProjectKey = jiraProject.get("key").asText();
				Dao dao = OneDev.getInstance(Dao.class);
				for (Issue issue: issues) {
					if (issue.getDescription() != null) 
						issue.setDescription(migrator.migratePrefixed(issue.getDescription(), jiraProjectKey + "-"));
					
					OneDev.getInstance(IssueManager.class).save(issue);
					for (IssueSchedule schedule: issue.getSchedules())
						dao.persist(schedule);
					for (IssueField field: issue.getFields())
						dao.persist(field);
					for (IssueComment comment: issue.getComments()) {
						comment.setContent(migrator.migratePrefixed(comment.getContent(),  jiraProjectKey + "-"));
						dao.persist(comment);
					}
				}
			}
			
			ImportResult result = new ImportResult();
			result.unmappedIssueStatuses.addAll(unmappedIssueStatuses);
			result.unmappedIssueTypes.addAll(unmappedIssueTypes);
			result.unmappedIssuePriorities.addAll(unmappedIssuePriorities);
			result.nonExistentLogins.addAll(nonExistentLogins);
			result.tooLargeAttachments.addAll(tooLargeAttachments);
			result.errorAttachments.addAll(errorAttachments);
			
			return result;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} finally {
			client.close();
		}
	}
	
	static ImportOption buildImportOption(ImportServer server, Collection<String> projectNames, TaskLogger logger) {
		ImportOption importOption = new ImportOption();
		
		Map<String, JsonNode> projectNodes = getProjectNodes(server, logger);
		
		Client client = server.newClient();
		try {
			String apiEndpoint = server.getApiEndpoint("/status");
			List<JsonNode> statusNodes = list(client, apiEndpoint, logger);
			
			Set<String> issueTypes = new LinkedHashSet<>();
			Set<String> issueStatuses = new LinkedHashSet<>();
			for (String projectName: projectNames) {
				JsonNode projectNode = projectNodes.get(projectName);
				if (projectNode == null)
					throw new ExplicitException("Unable to find project: " + projectName);
				apiEndpoint = server.getApiEndpoint("/issuetype/project?projectId=" + projectNode.get("id").asText()); 
				for (JsonNode issueTypeNode: list(client, apiEndpoint, logger))  
					issueTypes.add(issueTypeNode.get("untranslatedName").asText());
				if (projectNode.get("style").asText().equals("next-gen")) {
					for (JsonNode statusNode: statusNodes) {
						JsonNode scopeNode = statusNode.get("scope");
						if (scopeNode != null && scopeNode.get("project").get("id").asText().equals(projectNode.get("id").asText())) 
							issueStatuses.add(statusNode.get("untranslatedName").asText());
					}
				} else {
					for (JsonNode statusNode: statusNodes) {
						if (statusNode.get("scope") == null)  
							issueStatuses.add(statusNode.get("untranslatedName").asText());
					}
				}
			}
			for (String issueType: issueTypes) {
				IssueTypeMapping mapping = new IssueTypeMapping();
				mapping.setJiraIssueType(issueType);
				importOption.getIssueTypeMappings().add(mapping);
			}
			for (String issueStatus: issueStatuses) {
				IssueStatusMapping mapping = new IssueStatusMapping();
				mapping.setJiraIssueStatus(issueStatus);
				importOption.getIssueStatusMappings().add(mapping);
			}
			apiEndpoint = server.getApiEndpoint("/priority");
			for (JsonNode priorityNode: list(client, apiEndpoint, logger)) {
				IssuePriorityMapping mapping = new IssuePriorityMapping();
				mapping.setJiraIssuePriority(priorityNode.get("name").asText());
				importOption.getIssuePriorityMappings().add(mapping);
			}
		} finally {
			client.close();
		}
		return importOption;
	}
	
	static Map<String, JsonNode> getProjectNodes(ImportServer where, TaskLogger logger) {
		Map<String, JsonNode> projectNodes = new LinkedHashMap<>();
		
		Client client = where.newClient();
		try {
			for (JsonNode projectNode: list(client, where.getApiEndpoint("/project/search"), logger)) 
				projectNodes.put(projectNode.get("name").asText(), projectNode);
		} finally {
			client.close();
		}
		return projectNodes;
	}
	
	static List<JsonNode> list(Client client, String apiEndpoint, TaskLogger logger) {
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
			TaskLogger logger) {
		URI uri;
		try {
			uri = new URIBuilder(apiEndpoint)
					.addParameter("maxResults", String.valueOf(PER_PAGE)).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		int page = 0;
		while (true) {
			try {
				URIBuilder builder = new URIBuilder(uri);
				builder.addParameter("startAt", String.valueOf(page*PER_PAGE));
				List<JsonNode> pageData = new ArrayList<>();

				JsonNode resultNode = JerseyUtils.get(client, builder.build().toString(), logger);
				if (resultNode.isArray()) {
					for (JsonNode each: resultNode)
						pageData.add(each);
				} else {
					if (resultNode.hasNonNull("values")) {
						for (JsonNode each: resultNode.get("values"))
							pageData.add(each);
					} else if (resultNode.hasNonNull("issues")) {
						for (JsonNode each: resultNode.get("issues"))
							pageData.add(each);
					} else if (resultNode.hasNonNull("comments")) {
						for (JsonNode each: resultNode.get("comments"))
							pageData.add(each);
					}
				}
				pageDataConsumer.consume(pageData);
				if (pageData.size() < PER_PAGE)
					break;
				page++;
			} catch (URISyntaxException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
