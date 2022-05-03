package io.onedev.server.plugin.imports.youtrack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.field.spec.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.DateTimeField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.FloatField;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.model.support.issue.field.spec.TextField;
import io.onedev.server.model.support.issue.field.spec.UserChoiceField;
import io.onedev.server.model.support.issue.field.spec.WorkingPeriodField;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.JerseyUtils;
import io.onedev.server.util.JerseyUtils.PageDataConsumer;
import io.onedev.server.util.Pair;

public class ImportUtils {

	static final String NAME = "YouTrack";

	static final int PER_PAGE = 50;
	
	static ImportOption buildImportOption(ImportServer server, Collection<JsonNode> projectNodes, TaskLogger logger) {
		ImportOption importOption = new ImportOption();
		Client client = server.newClient();
		try {
			Set<String> youTrackIssueStates = new LinkedHashSet<>();
			Set<String> youTrackIssueFields = new LinkedHashSet<>();
			
			for (JsonNode projectNode: projectNodes) {
				for (JsonNode customFieldNode: projectNode.get("customFields")) {
					String fieldName = customFieldNode.get("field").get("name").asText();
					JsonNode bundleNode = customFieldNode.get("bundle");
					if (bundleNode != null) {
						String bundleType = bundleNode.get("$type").asText();
						if (bundleType.equals("StateBundle")) {
							for (JsonNode valueNode: bundleNode.get("values")) 
								youTrackIssueStates.add(valueNode.get("name").asText());
						} else if (bundleType.equals("EnumBundle") || bundleType.equals("OwnedBundle")) {
							for (JsonNode valueNode: bundleNode.get("values")) 
								youTrackIssueFields.add(fieldName + "::" + valueNode.get("name").asText());
						} else {
							youTrackIssueFields.add(fieldName);
						}
					} else {
						youTrackIssueFields.add(fieldName);
					}
				}
			}
			
			for (String youTrackIssueState: youTrackIssueStates) {
				IssueStateMapping mapping = new IssueStateMapping();
				mapping.setYouTrackIssueState(youTrackIssueState);
				importOption.getIssueStateMappings().add(mapping);
			}
			
			for (String youTrackIssueField: youTrackIssueFields) {
				IssueFieldMapping mapping = new IssueFieldMapping();
				mapping.setYouTrackIssueField(youTrackIssueField);
				importOption.getIssueFieldMappings().add(mapping);
			}

			String apiEndpoint = server.getApiEndpoint("/issueTags?fields=name");
			for (JsonNode tagNode: list(client, apiEndpoint, logger)) {
				IssueTagMapping mapping = new IssueTagMapping();
				mapping.setYouTrackIssueTag(tagNode.get("name").asText());
				importOption.getIssueTagMappings().add(mapping);
			}
			
			apiEndpoint = server.getApiEndpoint("/issueLinkTypes?fields=name");
			for (JsonNode tagNode: list(client, apiEndpoint, logger)) {
				IssueLinkMapping mapping = new IssueLinkMapping();
				mapping.setYouTrackIssueLink(tagNode.get("name").asText());
				importOption.getIssueLinkMappings().add(mapping);
			}
			
		} finally {
			client.close();
		}
		return importOption;
	}
	
	static ImportResult importIssues(ImportServer server, String youTrackProjectId, 
			Project oneDevProject, boolean retainIssueNumbers, ImportOption importOption, boolean dryRun, TaskLogger logger) {
		Client client = server.newClient();
		try {
			String apiEndpoint = server.getApiEndpoint("/admin/projects/" + youTrackProjectId + "?fields=shortName");
			String youTrackProjectShortName = JerseyUtils.get(client, apiEndpoint, logger).get("shortName").asText();
			
			Map<String, String> stateMappings = new HashMap<>();
			Map<String, Pair<FieldSpec, String>> fieldMappings = new HashMap<>(); 
			Map<String, Pair<FieldSpec, String>> tagMappings = new HashMap<>();
			Map<String, LinkSpec> linkMappings = new HashMap<>();
			
			Map<Long, Long> issueNumberMappings = new HashMap<>();
			Map<Long, Issue> issueMappings = new HashMap<>();
			
			for (IssueStateMapping mapping: importOption.getIssueStateMappings())
				stateMappings.put(mapping.getYouTrackIssueState(), mapping.getOneDevIssueState());
			
			for (IssueFieldMapping mapping: importOption.getIssueFieldMappings()) {
				String oneDevFieldName;
				String oneDevFieldValue;
				if (mapping.getOneDevIssueField().contains("::")) { 
					oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
					oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				} else {
					oneDevFieldName = mapping.getOneDevIssueField();
					oneDevFieldValue = null;
				}
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				fieldMappings.put(mapping.getYouTrackIssueField(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			for (IssueTagMapping mapping: importOption.getIssueTagMappings()) {
				String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
				String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				tagMappings.put(mapping.getYouTrackIssueTag(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			for (IssueLinkMapping mapping: importOption.getIssueLinkMappings()) {
				LinkSpec linkSpec = OneDev.getInstance(LinkSpecManager.class).find(mapping.getOneDevIssueLink());
				if (linkSpec == null)
					throw new ExplicitException("No link spec found: " + mapping.getOneDevIssueLink());
				linkMappings.put(mapping.getYouTrackIssueLink(), linkSpec);
			}

			Set<String> nonExistentLogins = new LinkedHashSet<>();
			Set<String> unmappedIssueTags = new LinkedHashSet<>();
			Set<String> unmappedIssueFields = new LinkedHashSet<>();
			Set<String> unmappedIssueLinks = new LinkedHashSet<>();
			Set<String> unmappedIssueStates = new LinkedHashSet<>();
			Map<String, String> mismatchedIssueFields = new LinkedHashMap<>();
			Set<String> tooLargeAttachments = new LinkedHashSet<>();
			
			AtomicInteger numOfImportedIssues = new AtomicInteger(0);
			
			List<Issue> issues = new ArrayList<>();
			Map<Long, Pair<LinkSpec, List<Long>>> issueLinkInfo = new HashMap<>();
			Map<Long, Set<Set<Long>>> processedSymmetricLinks = new HashMap<>();
			
			String fields = ""
					+ "idReadable,"
					+ "numberInProject,"
					+ "summary,"
					+ "description,"
					+ "created,"
					+ "comments(created,text,author(login,name,email),attachments(name,url,size)),"
					+ "attachments(name,url,comment,size),"
					+ "reporter(login,name,email),"
					+ "tags(name),"
					+ "customFields(name,value(name,login,email,presentation,text),projectCustomField(field(fieldType(id)))),"
					+ "links(direction,linkType(name,sourceToTarget,targetToSource),issues(numberInProject))";  
			PageDataConsumer pageDataConsumer = new PageDataConsumer() {

				@Nullable
				private String processAttachments(String issueUUID, String readableIssueId, @Nullable String markdown, 
						List<JsonNode> attachmentNodes, Set<String> tooLargeAttachments) {
					if (markdown == null)
						markdown = "";
					
					Map<String, String> unreferencedAttachments = new LinkedHashMap<>();
					
					long maxUploadFileSize = OneDev.getInstance(SettingManager.class)
							.getPerformanceSetting().getMaxUploadFileSize()*1L*1024*1024; 
					for (JsonNode attachmentNode: attachmentNodes) {
						String attachmentName = attachmentNode.get("name").asText(null);
						String attachmentUrl = attachmentNode.get("url").asText(null);
						long attachmentSize = attachmentNode.get("size").asLong(0);
						if (attachmentSize != 0 && attachmentName != null && attachmentUrl != null) {
							if (attachmentSize >  maxUploadFileSize) {
								tooLargeAttachments.add(readableIssueId + ":" + attachmentName);
							} else {
								if (!attachmentUrl.startsWith("/api"))
									throw new ExplicitException("Unexpected attachment url: " + attachmentUrl);
								attachmentUrl = attachmentUrl.substring("/api".length());

								String endpoint = server.getApiEndpoint(attachmentUrl);
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
										String oneDevAttachmentName = oneDevProject.saveAttachment(issueUUID, attachmentName, is);
										String oneDevAttachmentUrl = oneDevProject.getAttachmentUrlPath(issueUUID, oneDevAttachmentName);
										if (markdown.contains("(" + attachmentName + ")")) { 
											markdown = markdown.replace("(" + attachmentName + ")", "(" + oneDevAttachmentUrl + ")");
										} else { 
											unreferencedAttachments.put(attachmentName, oneDevAttachmentUrl);
										}
									} catch (IOException e) {
										throw new RuntimeException(e);
									} 
								}
							}
						}
					}
					
					if (!unreferencedAttachments.isEmpty()) {  
						markdown += "\n\n";
						for (Map.Entry<String, String> entry: unreferencedAttachments.entrySet()) 
							markdown += "[" + entry.getKey() + "](" + entry.getValue() + ") ";
					}

					if (markdown.length() == 0)
						markdown = null;
					
					return markdown;
				}
				
				private String joinAsMultilineHtml(List<String> values) {
					List<String> escapedValues = new ArrayList<>();
					for (String value: values)
						escapedValues.add(HtmlEscape.escapeHtml5(value));
					return StringUtils.join(escapedValues, "<br>");
				}
				
				@Nullable
				private String getEmail(JsonNode userNode) {
					JsonNode emailNode = userNode.get("email");
					if (emailNode != null)
						return emailNode.asText(null);
					else
						return null;
				}
				
				@Override
				public void consume(List<JsonNode> pageData) throws InterruptedException {
					for (JsonNode issueNode: pageData) {
						if (Thread.interrupted())
							throw new InterruptedException();
						
						Issue issue = new Issue();
						String readableId = issueNode.get("idReadable").asText();
						Long oldNumber = issueNode.get("numberInProject").asLong();
						Long newNumber;
						if (dryRun || retainIssueNumbers)
							newNumber = oldNumber;
						else
							newNumber = getIssueManager().getNextNumber(oneDevProject);
						issue.setNumber(newNumber);
						issueNumberMappings.put(oldNumber, newNumber);
						issueMappings.put(oldNumber, issue);
						issue.setTitle(issueNode.get("summary").asText());
						issue.setDescription(issueNode.get("description").asText(null));
						issue.setSubmitDate(new Date(issueNode.get("created").asLong(System.currentTimeMillis())));
						issue.setProject(oneDevProject);
						issue.setNumberScope(oneDevProject.getForkRoot());
						
						if (!dryRun) {
							List<JsonNode> attachmentNodes = new ArrayList<>();
							for (JsonNode attachmentNode: issueNode.get("attachments")) {
								if (!attachmentNode.hasNonNull("comment"))
									attachmentNodes.add(attachmentNode);
							}
							issue.setDescription(processAttachments(issue.getUUID(), readableId, 
									issue.getDescription(), attachmentNodes, tooLargeAttachments));
						}
						
						Map<String, String> extraIssueInfo = new LinkedHashMap<>();
						
						if (issueNode.hasNonNull("reporter")) {
							JsonNode reporterNode = issueNode.get("reporter");
							String email = getEmail(reporterNode);
							String login = reporterNode.get("login").asText();
							if (email != null) {
								User user = OneDev.getInstance(UserManager.class).findByVerifiedEmailAddress(email);
								if (user != null) {
									issue.setSubmitter(user);
								} else {
									issue.setSubmitter(OneDev.getInstance(UserManager.class).getUnknown());
									nonExistentLogins.add(login);
								}
							} else {
								issue.setSubmitter(OneDev.getInstance(UserManager.class).getUnknown());
								nonExistentLogins.add(login);
							}
						} else {
							issue.setSubmitter(SecurityUtils.getUser());
						}
						
						LastUpdate lastUpdate = new LastUpdate();
						lastUpdate.setActivity("Opened");
						lastUpdate.setDate(issue.getSubmitDate());
						lastUpdate.setUser(issue.getSubmitter());
						issue.setLastUpdate(lastUpdate);
						
						StateSpec initialState = getIssueSetting().getInitialStateSpec();
						for (JsonNode customFieldNode: issueNode.get("customFields")) {
							String fieldName = customFieldNode.get("name").asText();
							String fieldType = customFieldNode.get("$type").asText();
							switch (fieldType) {
							case "StateIssueCustomField": 
								if (customFieldNode.hasNonNull("value")) {
									String state = customFieldNode.get("value").get("name").asText();
									String mappedState = stateMappings.get(state);
									if (mappedState != null) 
										issue.setState(mappedState);
									else
										unmappedIssueStates.add(state);
								}
								break;
							case "SingleBuildIssueCustomField":
							case "SingleGroupIssueCustomField":
							case "SingleVersionIssueCustomField":
								String fieldValue = customFieldNode.get("value").asText(null);
								if (fieldValue != null) {
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
									if (mapped == null) {
										unmappedIssueFields.add(fieldName);
										extraIssueInfo.put(fieldName, fieldValue);
									} else if (!(mapped.getFirst() instanceof TextField)) {
										mismatchedIssueFields.put(fieldName, "Should be mapped to a text field");
										extraIssueInfo.put(fieldName, fieldValue);
									} else {
										IssueField issueField = new IssueField();
										issueField.setIssue(issue);
										issueField.setName(mapped.getFirst().getName());
										issueField.setType(InputSpec.TEXT);
										issueField.setValue(fieldValue);
										issue.getFields().add(issueField);
									}
								}
								break;
							case "MultiBuildIssueCustomField":
							case "MultiGroupIssueCustomField":
							case "MultiVersionIssueCustomField":
								List<String> fieldValues = new ArrayList<>();
								for (JsonNode valueNode: customFieldNode.get("value"))
									fieldValues.add(valueNode.get("name").asText());
								if (!fieldValues.isEmpty()) {
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
									if (mapped == null) {
										unmappedIssueFields.add(fieldName);
										extraIssueInfo.put(fieldName, joinAsMultilineHtml(fieldValues));
									} else if (!(mapped.getFirst() instanceof TextField)) {
										mismatchedIssueFields.put(fieldName, "Should be mapped to a text field");
										extraIssueInfo.put(fieldName, joinAsMultilineHtml(fieldValues));
									} else {
										IssueField issueField = new IssueField();
										issueField.setIssue(issue);
										issueField.setName(mapped.getFirst().getName());
										issueField.setType(InputSpec.TEXT);
										issueField.setValue(StringUtils.join(fieldValues, ", "));
										issue.getFields().add(issueField);
									}
								}
								break;
							case "SingleEnumIssueCustomField":
							case "SingleOwnedIssueCustomField":
								if (customFieldNode.hasNonNull("value")) {
									fieldValue = customFieldNode.get("value").get("name").asText();
									String fieldNameWithValue = fieldName + "::" + fieldValue;
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldNameWithValue);
									if (mapped == null) {
										unmappedIssueFields.add(fieldNameWithValue);
										extraIssueInfo.put(fieldName, fieldValue);
									} else if (!(mapped.getFirst() instanceof ChoiceField) || mapped.getFirst().isAllowMultiple()) {
										mismatchedIssueFields.put(fieldNameWithValue, "Should be mapped to a single-valued enum field");
										extraIssueInfo.put(fieldName, fieldValue);
									} else {
										IssueField issueField = new IssueField();
										issueField.setIssue(issue);
										issueField.setName(mapped.getFirst().getName());
										issueField.setType(InputSpec.ENUMERATION);
										issueField.setValue(mapped.getSecond());
										issue.getFields().add(issueField);
									}
								}
								break;
							case "MultiEnumIssueCustomField":
							case "MultiOwnedIssueCustomField":
								fieldValues = new ArrayList<>();
								for (JsonNode valueNode: customFieldNode.get("value")) 
									fieldValues.add(valueNode.get("name").asText());

								boolean hasErrors = false;
								List<IssueField> issueFields = new ArrayList<>();
								for (String each: fieldValues) {
									String fieldNameWithValue = fieldName + "::" + each;
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldNameWithValue);
									if (mapped == null) {
										unmappedIssueFields.add(fieldNameWithValue);
										hasErrors = true;
									} else if (!(mapped.getFirst() instanceof ChoiceField) || !mapped.getFirst().isAllowMultiple()) {
										mismatchedIssueFields.put(fieldNameWithValue, "Should be mapped to a multi-valued enum field");
										hasErrors = true;
									} else {
										IssueField issueField = new IssueField();
										issueField.setIssue(issue);
										issueField.setName(mapped.getFirst().getName());
										issueField.setType(InputSpec.ENUMERATION);
										issueField.setValue(mapped.getSecond());
										issueFields.add(issueField);
									}
								}
								if (hasErrors) 
									extraIssueInfo.put(fieldName, joinAsMultilineHtml(fieldValues));
								else
									issue.getFields().addAll(issueFields);
								break;
							case "SingleUserIssueCustomField":
								if (customFieldNode.hasNonNull("value")) {
									JsonNode valueNode = customFieldNode.get("value");
									
									String login = valueNode.get("login").asText();
									String fullName = valueNode.get("name").asText();
									String email = getEmail(valueNode);
									
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
									if (mapped == null) {
										unmappedIssueFields.add(fieldName);
										extraIssueInfo.put(fieldName, fullName);
									} else if (!(mapped.getFirst() instanceof UserChoiceField) || mapped.getFirst().isAllowMultiple()) {
										mismatchedIssueFields.put(fieldName, "Should be mapped to a single-valued user field");
										extraIssueInfo.put(fieldName, fullName);
									} else {
										if (email != null) {
											User user = OneDev.getInstance(UserManager.class).findByVerifiedEmailAddress(email);
											if (user != null) {
												fieldValue = user.getName();
											} else {
												fieldValue = null;
												nonExistentLogins.add(login);
											}
										} else {
											fieldValue = null;
											nonExistentLogins.add(login);
										}
										
										if (fieldValue != null) {
											IssueField issueField = new IssueField();
											issueField.setIssue(issue);
											issueField.setName(mapped.getFirst().getName());
											issueField.setType(InputSpec.USER);
											issueField.setValue(fieldValue);
											issue.getFields().add(issueField);
										}
									}
								}
								break;
							case "MultiUserIssueCustomField":
								List<String> fullNames = new ArrayList<>();
								for (JsonNode valueNode: customFieldNode.get("value")) 
									fullNames.add(valueNode.get("name").asText());
								if (!fullNames.isEmpty()) {
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
									if (mapped == null) {
										unmappedIssueFields.add(fieldName);
										extraIssueInfo.put(fieldName, joinAsMultilineHtml(fullNames));
									} else if (!(mapped.getFirst() instanceof UserChoiceField) || !mapped.getFirst().isAllowMultiple()) {
										mismatchedIssueFields.put(fieldName, "Should be mapped to a multi-valued user field");
										extraIssueInfo.put(fieldName, joinAsMultilineHtml(fullNames));
									} else {
										for (JsonNode valueNode: customFieldNode.get("value")) {
											String login = valueNode.get("login").asText();
											String email = getEmail(valueNode);
											
											if (email != null) {
												User user = OneDev.getInstance(UserManager.class).findByVerifiedEmailAddress(email);
												if (user != null) {
													fieldValue = user.getName();
												} else {
													fieldValue = null;
													nonExistentLogins.add(login);
												}
											} else {
												fieldValue = null;
												nonExistentLogins.add(login);
											}

											if (fieldValue != null) {
												IssueField issueField = new IssueField();
												issueField.setIssue(issue);
												issueField.setName(mapped.getFirst().getName());
												issueField.setType(InputSpec.USER);
												issueField.setValue(fieldValue);
												issue.getFields().add(issueField);
											}
										}
									}
								}
								break;	
							case "DateIssueCustomField":
								if (customFieldNode.hasNonNull("value")) {
									Date date = new Date(customFieldNode.get("value").asLong());
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
									if (mapped == null) {
										unmappedIssueFields.add(fieldName);
										extraIssueInfo.put(fieldName, DateUtils.formatDate(date));
									} else if (!(mapped.getFirst() instanceof DateField)) {
										mismatchedIssueFields.put(fieldName, "Should be mapped to a date field");
										extraIssueInfo.put(fieldName, DateUtils.formatDate(date));
									} else {
										IssueField issueField = new IssueField();
										issueField.setIssue(issue);
										issueField.setName(mapped.getFirst().getName());
										issueField.setType(InputSpec.DATE);
										issueField.setValue(DateUtils.formatDate(date));
										issue.getFields().add(issueField);
									}
								}
								break;
							case "SimpleIssueCustomField":
								if (customFieldNode.hasNonNull("value")) {
									JsonNode projectCustomField = customFieldNode.get("projectCustomField");
									JsonNode fieldNode = projectCustomField.get("field");
									if (fieldNode != null) {
										switch (fieldNode.get("fieldType").get("id").asText()) {
										case "date":
											Date date = new Date(customFieldNode.get("value").asLong());
											Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
											if (mapped == null) {
												unmappedIssueFields.add(fieldName);
												extraIssueInfo.put(fieldName, DateUtils.formatDateTime(date));
											} else if (!(mapped.getFirst() instanceof DateTimeField)) {
												mismatchedIssueFields.put(fieldName, "Should be mapped to a datetime field");
												extraIssueInfo.put(fieldName, DateUtils.formatDateTime(date));
											} else {
												IssueField issueField = new IssueField();
												issueField.setIssue(issue);
												issueField.setName(mapped.getFirst().getName());
												issueField.setType(InputSpec.DATE_TIME);
												issueField.setValue(DateUtils.formatDateTime(date));
												issue.getFields().add(issueField);
											}
											break;
										case "float":
											fieldValue = customFieldNode.get("value").asText();
											mapped = fieldMappings.get(fieldName);
											if (mapped == null) {
												unmappedIssueFields.add(fieldName);
												extraIssueInfo.put(fieldName, fieldValue);
											} else if (!(mapped.getFirst() instanceof FloatField)) {
												mismatchedIssueFields.put(fieldName, "Should be mapped to a float field");
												extraIssueInfo.put(fieldName, fieldValue);
											} else {
												IssueField issueField = new IssueField();
												issueField.setIssue(issue);
												issueField.setName(mapped.getFirst().getName());
												issueField.setType(InputSpec.FLOAT);
												issueField.setValue(fieldValue);
												issue.getFields().add(issueField);
											}
											break;
										case "integer":
											fieldValue = customFieldNode.get("value").asText();
											mapped = fieldMappings.get(fieldName);
											if (mapped == null) {
												unmappedIssueFields.add(fieldName);
												extraIssueInfo.put(fieldName, fieldValue);
											} else if (!(mapped.getFirst() instanceof IntegerField)) {
												mismatchedIssueFields.put(fieldName, "Should be mapped to an integer field");
												extraIssueInfo.put(fieldName, fieldValue);
											} else {
												IssueField issueField = new IssueField();
												issueField.setIssue(issue);
												issueField.setName(mapped.getFirst().getName());
												issueField.setType(InputSpec.INTEGER);
												issueField.setValue(fieldValue);
												issue.getFields().add(issueField);
											}
											break;
										case "string":
											fieldValue = customFieldNode.get("value").asText();
											mapped = fieldMappings.get(fieldName);
											if (mapped == null) {
												unmappedIssueFields.add(fieldName);
												extraIssueInfo.put(fieldName, fieldValue);
											} else if (!(mapped.getFirst() instanceof TextField)) {
												mismatchedIssueFields.put(fieldName, "Should be mapped to a text field");
												extraIssueInfo.put(fieldName, fieldValue);
											} else {
												IssueField issueField = new IssueField();
												issueField.setIssue(issue);
												issueField.setName(mapped.getFirst().getName());
												issueField.setType(InputSpec.TEXT);
												issueField.setValue(fieldValue);
												issue.getFields().add(issueField);
											}
											break;
										}
									}
								}
								break;
							case "TextIssueCustomField":
								if (customFieldNode.hasNonNull("value")) {
									fieldValue = customFieldNode.get("value").get("text").asText();
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
									if (mapped == null) {
										unmappedIssueFields.add(fieldName);
										extraIssueInfo.put(fieldName, fieldValue);
									} else if (!(mapped.getFirst() instanceof TextField) 
											|| !((TextField)mapped.getFirst()).isMultiline()) {
										mismatchedIssueFields.put(fieldName, "Should be mapped to a multi-line text field");
										extraIssueInfo.put(fieldName, fieldValue);
									} else {
										IssueField issueField = new IssueField();
										issueField.setIssue(issue);
										issueField.setName(mapped.getFirst().getName());
										issueField.setType(InputSpec.TEXT);
										issueField.setValue(fieldValue);
										issue.getFields().add(issueField);
									}
								}
								break;
							case "PeriodIssueCustomField":
								if (customFieldNode.hasNonNull("value")) {
									fieldValue = customFieldNode.get("value").get("presentation").asText();
									Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
									if (mapped == null) {
										unmappedIssueFields.add(fieldName);
										extraIssueInfo.put(fieldName, fieldValue);
									} else if (!(mapped.getFirst() instanceof WorkingPeriodField)) {
										mismatchedIssueFields.put(fieldName, "Should be mapped to a working period field");
										extraIssueInfo.put(fieldName, fieldValue);
									} else {
										IssueField issueField = new IssueField();
										issueField.setIssue(issue);
										issueField.setName(mapped.getFirst().getName());
										issueField.setType(InputSpec.WORKING_PERIOD);
										issueField.setValue(fieldValue);
										issue.getFields().add(issueField);
									}
								}
								break;
							}
						}
						if (issue.getState() == null)
							issue.setState(initialState.getName());
						
						for (JsonNode linkNode: issueNode.get("links")) {
							List<Long> linkedIssueNumbers = new ArrayList<>();
							for (JsonNode linkedIssueNode: linkNode.get("issues")) 
								linkedIssueNumbers.add(linkedIssueNode.get("numberInProject").asLong());
							
							if (!linkedIssueNumbers.isEmpty() && linkNode.hasNonNull("linkType")) {
								JsonNode linkTypeNode = linkNode.get("linkType");
								String direction = linkNode.get("direction").asText();								
								String linkName = linkTypeNode.get("name").asText(null);
								if (linkName != null) {
									LinkSpec linkSpec = linkMappings.get(linkName);
									if (linkSpec == null) { 
										List<String> linkedIssueLinks = new ArrayList<>();
										for (Long issueNumber: linkedIssueNumbers)
											linkedIssueLinks.add(youTrackProjectShortName + "-" + issueNumber);
										
										unmappedIssueLinks.add(linkName);
										switch (direction) {
										case "BOTH":
										case "OUTWARD":
											linkName = linkTypeNode.get("sourceToTarget").asText(null);
											if (linkName != null) 
												extraIssueInfo.put(linkName, joinAsMultilineHtml(linkedIssueLinks));
											break;
										case "INWARD":
											linkName = linkTypeNode.get("targetToSource").asText(null);
											if (linkName != null) 
												extraIssueInfo.put(linkName, joinAsMultilineHtml(linkedIssueLinks));
											break;
										}
									} else if ("OUTWARD".equals(direction)) {
										issueLinkInfo.put(oldNumber, new Pair<>(linkSpec, linkedIssueNumbers));
									} else if ("BOTH".equals(direction)) {
										Set<Set<Long>> value = processedSymmetricLinks.get(linkSpec.getId());
										if (value == null) {
											value = new HashSet<>();
											processedSymmetricLinks.put(linkSpec.getId(), value);
										}
										List<Long> filteredIssueNumbers = new ArrayList<>();
										for (Long issueNumber: linkedIssueNumbers) {
											Set<Long> linkSides = Sets.newHashSet(oldNumber, issueNumber);
											if (value.add(linkSides))
												filteredIssueNumbers.add(issueNumber);
										}
										if (!filteredIssueNumbers.isEmpty())
											issueLinkInfo.put(oldNumber, new Pair<>(linkSpec, filteredIssueNumbers));
									}
								}
							}
						}
						
						List<String> currentUnmappedTags = new ArrayList<>();
						for (JsonNode tagNode: issueNode.get("tags")) {
							String tagName = tagNode.get("name").asText();
							Pair<FieldSpec, String> mapped = tagMappings.get(tagName);
							if (mapped != null) {
								IssueField tagField = new IssueField();
								tagField.setIssue(issue);
								tagField.setName(mapped.getFirst().getName());
								tagField.setType(InputSpec.ENUMERATION);
								tagField.setValue(mapped.getSecond());
								tagField.setOrdinal(mapped.getFirst().getOrdinal(mapped.getSecond()));
								issue.getFields().add(tagField);
							} else {
								currentUnmappedTags.add(HtmlEscape.escapeHtml5(tagName));
								unmappedIssueTags.add(HtmlEscape.escapeHtml5(tagName));
							}
						}
						if (!currentUnmappedTags.isEmpty()) 
							extraIssueInfo.put("Tags", joinAsMultilineHtml(currentUnmappedTags));
						
						for (JsonNode commentNode: issueNode.get("comments")) {
							String commentContent = commentNode.get("text").asText(null);
							if (commentContent != null || !commentNode.get("attachments").isEmpty()) {
								IssueComment comment = new IssueComment();
								comment.setIssue(issue);
								if (!dryRun) {
									List<JsonNode> attachmentNodes = new ArrayList<>();
									for (JsonNode attachmentNode: commentNode.get("attachments"))
										attachmentNodes.add(attachmentNode);
									comment.setContent(processAttachments(issue.getUUID(), readableId, 
											commentContent, attachmentNodes, tooLargeAttachments));
								}
								comment.setDate(new Date(commentNode.get("created").asLong(System.currentTimeMillis())));
								if (commentNode.hasNonNull("author")) {
									JsonNode authorNode = commentNode.get("author");
									String email = getEmail(authorNode);
									String login = authorNode.get("login").asText();
									if (email != null) {
										User user = OneDev.getInstance(UserManager.class).findByVerifiedEmailAddress(email);
										if (user != null) {
											comment.setUser(user);
										} else {
											comment.setUser(OneDev.getInstance(UserManager.class).getUnknown());
											nonExistentLogins.add(login);
										}
									} else {
										comment.setUser(OneDev.getInstance(UserManager.class).getUnknown());
										nonExistentLogins.add(login);
									}
								} else {
									issue.setSubmitter(SecurityUtils.getUser());
								}
								issue.getComments().add(comment);
							}
						}
						
						issue.setCommentCount(issue.getComments().size());
						
						Set<String> fieldAndValues = new HashSet<>();
						for (IssueField field: issue.getFields()) {
							String fieldAndValue = field.getName() + "::" + field.getValue();
							if (!fieldAndValues.add(fieldAndValue)) {
								String errorMessage = String.format(
										"Duplicate issue field mapping (issue: %s, field: %s)", 
										readableId, fieldAndValue);
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
			
			apiEndpoint = server.getApiEndpoint("/admin/projects/" + youTrackProjectId + "/issues?fields=" + fields);
			list(client, apiEndpoint, pageDataConsumer, logger);

			if (!dryRun) {
				ReferenceMigrator migrator = new ReferenceMigrator(Issue.class, issueNumberMappings);
				Dao dao = OneDev.getInstance(Dao.class);
				for (Issue issue: issues) {
					if (issue.getDescription() != null) 
						issue.setDescription(migrator.migratePrefixed(issue.getDescription(), youTrackProjectShortName + "-"));
					
					getIssueManager().save(issue);
					for (IssueSchedule schedule: issue.getSchedules())
						dao.persist(schedule);
					for (IssueField field: issue.getFields())
						dao.persist(field);
					for (IssueComment comment: issue.getComments()) {
						comment.setContent(migrator.migratePrefixed(comment.getContent(), youTrackProjectShortName + "-"));
						dao.persist(comment);
					}
				}
				
				for (Map.Entry<Long, Pair<LinkSpec, List<Long>>> entry: issueLinkInfo.entrySet()) {
					Issue source = issueMappings.get(entry.getKey());
					if (source != null) {
						for (Long targetNumber: entry.getValue().getSecond()) {
							Issue target = issueMappings.get(targetNumber);
							if (target != null) {
								IssueLink link = new IssueLink();
								link.setSource(source);
								link.setTarget(target);
								link.setSpec(entry.getValue().getFirst());
								OneDev.getInstance(IssueLinkManager.class).save(link);
							}
						}
					}
				}
			}
			
			ImportResult result = new ImportResult();
			result.mismatchedIssueFields.putAll(mismatchedIssueFields);
			result.nonExistentLogins.addAll(nonExistentLogins);
			result.tooLargeAttachments.addAll(tooLargeAttachments);
			result.unmappedIssueFields.addAll(unmappedIssueFields);
			result.unmappedIssueStates.addAll(unmappedIssueStates);
			result.unmappedIssueLinks.addAll(unmappedIssueLinks);
			result.unmappedIssueTags.addAll(unmappedIssueTags);
			
			return result;
		} finally {
			client.close();
		}
	}
	
	private static IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
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
	
	static void list(Client client, String apiEndpoint, PageDataConsumer pageDataConsumer, TaskLogger logger) {
		URI uri;
		try {
			uri = new URIBuilder(apiEndpoint)
					.addParameter("$top", String.valueOf(PER_PAGE)).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		int page = 0;
		while (true) {
			try {
				URIBuilder builder = new URIBuilder(uri);
				builder.addParameter("$skip", String.valueOf(page*PER_PAGE));
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
	
}