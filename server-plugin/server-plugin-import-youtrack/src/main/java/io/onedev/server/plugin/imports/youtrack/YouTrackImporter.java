package io.onedev.server.plugin.imports.youtrack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
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
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Pair;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.page.project.imports.ProjectImporter;

public class YouTrackImporter extends ProjectImporter<YouTrackImportSource, YouTrackImportOption> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "YouTrack";

	private static final int PER_PAGE = 25;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public YouTrackImportOption initImportOption(YouTrackImportSource importSource, SimpleLogger logger) {
		YouTrackImportOption importOption = new YouTrackImportOption();
		Client client = newClient(importSource);
		try {
			String fields = "name,customFields(field(name),bundle(values(name)))";
			
			Set<String> youTrackIssueStates = new LinkedHashSet<>();
			Set<String> youTrackIssueFields = new LinkedHashSet<>();
			
			for (JsonNode projectNode: list(client, importSource, "/admin/projects?fields=" + fields, logger)) {
				YouTrackImport aImport = new YouTrackImport();
				aImport.setYouTrackProject(projectNode.get("name").asText());
				aImport.setOneDevProject(aImport.getYouTrackProject().replace(' ', '-'));
				importOption.getImports().add(aImport);
				
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
			
			for (JsonNode tagNode: list(client, importSource, "/issueTags?fields=name", logger)) {
				IssueTagMapping mapping = new IssueTagMapping();
				mapping.setYouTrackIssueTag(tagNode.get("name").asText());
				importOption.getIssueTagMappings().add(mapping);
			}
			
		} finally {
			client.close();
		}
		return importOption;
	}
	
	@Override
	public String doImport(YouTrackImportSource importSource, YouTrackImportOption importOption, boolean dryRun,
			SimpleLogger logger) {
		Collection<Long> projectIds = new ArrayList<>();
		Client client = newClient(importSource);
		try {
			Map<String, String> stateMappings = new HashMap<>();
			Map<String, Pair<FieldSpec, String>> fieldMappings = new HashMap<>(); 
			Map<String, Pair<FieldSpec, String>> tagMappings = new HashMap<>();
			
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

			Map<String, String> youTrackProjectIds = new HashMap<>();
			Map<String, String> projectDescriptions = new HashMap<>();
			for (JsonNode projectNode: list(client, importSource, "/admin/projects?fields=id,name,description", logger)) {
				String projectName = projectNode.get("name").asText();
				youTrackProjectIds.put(projectName, projectNode.get("id").asText());
				projectDescriptions.put(projectName, projectNode.get("description").asText(null));
			}				
			
			Set<String> unmappedAccounts = new LinkedHashSet<>();
			Set<String> unmappedIssueTags = new LinkedHashSet<>();
			Set<String> unmappedIssueFields = new LinkedHashSet<>();
			Set<String> unmappedIssueStates = new LinkedHashSet<>();
			Map<String, String> mismatchedIssueFields = new LinkedHashMap<>();
			Set<String> tooLargeAttachments = new LinkedHashSet<>();
			
			for (YouTrackImport aImport: importOption.getImports()) {
				logger.log("Importing project '" + aImport.getYouTrackProject() + "'...");
				
				String youTrackProjectId = Preconditions.checkNotNull(youTrackProjectIds.get(aImport.getYouTrackProject()));
				
				Project project = new Project();
				project.setName(aImport.getOneDevProject());
				project.setDescription(projectDescriptions.get(aImport.getYouTrackProject()));
				project.setIssueManagementEnabled(true);
				
				User user = SecurityUtils.getUser();
		       	UserAuthorization authorization = new UserAuthorization();
		       	authorization.setProject(project);
		       	authorization.setUser(user);
		       	authorization.setRole(OneDev.getInstance(RoleManager.class).getOwner());
		       	project.getUserAuthorizations().add(authorization);
		       	user.getAuthorizations().add(authorization);

		       	if (!dryRun) {
					OneDev.getInstance(ProjectManager.class).save(project);
					projectIds.add(project.getId());
					OneDev.getInstance(UserAuthorizationManager.class).save(authorization);
		       	}
				
				AtomicInteger numOfImportedIssues = new AtomicInteger(0);
				
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
						+ "links(direction,linkType(sourceToTarget,targetToSource),issues(numberInProject))";  
				PageDataConsumer pageDataConsumer = new PageDataConsumer() {

					@Nullable
					private String processAttachments(String issueUUID, String readableIssueId, @Nullable String markdown, 
							List<JsonNode> attachmentNodes, Set<String> tooLargeAttachments) {
						if (markdown == null)
							markdown = "";
						
						Map<String, String> unreferencedAttachments = new LinkedHashMap<>();
						
						for (JsonNode attachmentNode: attachmentNodes) {
							String attachmentName = attachmentNode.get("name").asText(null);
							String attachmentUrl = attachmentNode.get("url").asText(null);
							long attachmentSize = attachmentNode.get("size").asLong(0);
							if (attachmentSize != 0 && attachmentName != null && attachmentUrl != null) {
								if (attachmentSize >  Project.MAX_ATTACHMENT_SIZE) {
									tooLargeAttachments.add(readableIssueId + ":" + attachmentName);
								} else {
									if (!attachmentUrl.startsWith("/api"))
										throw new ExplicitException("Unexpected attachment url: " + attachmentUrl);
									attachmentUrl = attachmentUrl.substring("/api".length());
									
									WebTarget target = client.target(importSource.getApiEndpoint(attachmentUrl));
									Invocation.Builder builder =  target.request();
									try (Response response = builder.get()) {
										int status = response.getStatus();
										if (status != 200) {
											String errorMessage = response.readEntity(String.class);
											if (StringUtils.isNotBlank(errorMessage)) {
												throw new ExplicitException("Error downloading attachment: " + errorMessage);
											} else {
												throw new RuntimeException("Attachment download failed with status " + status 
														+ ", check server log for detaiils");
											}
										} else {
											try (InputStream is = response.readEntity(InputStream.class)) {
												String oneDevAttachmentName = project.saveAttachment(issueUUID, attachmentName, is);
												String oneDevAttachmentUrl = project.getAttachmentUrlPath(issueUUID, oneDevAttachmentName);
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
					
					@Override
					public void consume(List<JsonNode> pageData) throws InterruptedException {
						for (JsonNode issueNode: pageData) {
							Issue issue = new Issue();
							String readableId = issueNode.get("idReadable").asText();
							issue.setNumber(issueNode.get("numberInProject").asLong());
							issue.setTitle(issueNode.get("summary").asText());
							issue.setDescription(issueNode.get("description").asText(null));
							issue.setSubmitDate(new Date(issueNode.get("created").asLong(System.currentTimeMillis())));
							issue.setProject(project);
							issue.setNumberScope(project);
							
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
								String email = reporterNode.get("email").asText(null);
								String fullName = reporterNode.get("name").asText();
								String login = reporterNode.get("login").asText();
								if (email != null) {
									User user = OneDev.getInstance(UserManager.class).findByEmail(email);
									if (user != null) {
										issue.setSubmitter(user);
									} else {
										issue.setSubmitterName(fullName);
										unmappedAccounts.add(login);
									}
								} else {
									issue.setSubmitterName(fullName);
									unmappedAccounts.add(login);
								}
							} else {
								issue.setSubmitter(SecurityUtils.getUser());
							}
							
							LastUpdate lastUpdate = new LastUpdate();
							lastUpdate.setActivity("Opened");
							lastUpdate.setDate(issue.getSubmitDate());
							lastUpdate.setUser(issue.getSubmitter());
							lastUpdate.setUserName(issue.getSubmitterName());
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
										String email = valueNode.get("email").asText(null);
										
										Pair<FieldSpec, String> mapped = fieldMappings.get(fieldName);
										if (mapped == null) {
											unmappedIssueFields.add(fieldName);
											extraIssueInfo.put(fieldName, fullName);
										} else if (!(mapped.getFirst() instanceof UserChoiceField) || mapped.getFirst().isAllowMultiple()) {
											mismatchedIssueFields.put(fieldName, "Should be mapped to a single-valued user field");
											extraIssueInfo.put(fieldName, fullName);
										} else {
											if (email != null) {
												User user = OneDev.getInstance(UserManager.class).findByEmail(email);
												if (user != null) {
													fieldValue = user.getName();
												} else {
													fieldValue = fullName;
													unmappedAccounts.add(login);
												}
											} else {
												fieldValue = fullName;
												unmappedAccounts.add(login);
											}
											
											IssueField issueField = new IssueField();
											issueField.setIssue(issue);
											issueField.setName(mapped.getFirst().getName());
											issueField.setType(InputSpec.USER);
											issueField.setValue(fieldValue);
											issue.getFields().add(issueField);
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
												String fullName = valueNode.get("name").asText();
												String email = valueNode.get("email").asText(null);
												
												if (email != null) {
													User user = OneDev.getInstance(UserManager.class).findByEmail(email);
													if (user != null) {
														fieldValue = user.getName();
													} else {
														fieldValue = fullName;
														unmappedAccounts.add(login);
													}
												} else {
													fieldValue = fullName;
													unmappedAccounts.add(login);
												}
												
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
								List<String> linkedIssueLinks = new ArrayList<>();
								for (JsonNode linkedIssueNode: linkNode.get("issues")) 
									linkedIssueLinks.add("issue #" + linkedIssueNode.get("numberInProject").asLong());
								
								if (!linkedIssueLinks.isEmpty() && linkNode.hasNonNull("linkType")) {
									JsonNode linkTypeNode = linkNode.get("linkType");
									switch (linkNode.get("direction").asText()) {
									case "BOTH":
									case "OUTWARD":
										String linkName = linkTypeNode.get("sourceToTarget").asText(null);
										if (linkName != null) 
											extraIssueInfo.put(linkName, joinAsMultilineHtml(linkedIssueLinks));
										break;
									case "INWARD":
										linkName = linkTypeNode.get("targetToSource").asText(null);
										if (linkName != null) 
											extraIssueInfo.put(linkName, joinAsMultilineHtml(linkedIssueLinks));
										break;
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
										String email = authorNode.get("email").asText(null);
										String fullName = authorNode.get("name").asText();
										String login = authorNode.get("login").asText();
										if (email != null) {
											User user = OneDev.getInstance(UserManager.class).findByEmail(email);
											if (user != null) {
												comment.setUser(user);
											} else {
												comment.setUserName(fullName);
												unmappedAccounts.add(login);
											}
										} else {
											comment.setUserName(fullName);
											unmappedAccounts.add(login);
										}
									} else {
										issue.setSubmitter(SecurityUtils.getUser());
									}
									issue.getComments().add(comment);
								}
							}
							
							issue.setCommentCount(issue.getComments().size());
							
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
									OneDev.getInstance(IssueFieldManager.class).save(field);
								for (IssueComment comment: issue.getComments())
									OneDev.getInstance(IssueCommentManager.class).save(comment);
							}
						}
						logger.log("Imported " + numOfImportedIssues.addAndGet(pageData.size()) + " issues");
					}
					
				};
				list(client, importSource, "/admin/projects/" + youTrackProjectId + "/issues?fields=" + fields, 
						pageDataConsumer, logger);
			}
			StringBuilder feedback = new StringBuilder("Projects imported successfully");
			
			boolean hasNotes = 
					!unmappedIssueStates.isEmpty() 
					|| !unmappedIssueFields.isEmpty() 
					|| !mismatchedIssueFields.isEmpty() 
					|| !unmappedIssueTags.isEmpty()
					|| !unmappedAccounts.isEmpty()
					|| !tooLargeAttachments.isEmpty();

			if (hasNotes)
				feedback.append("<br><br><b>NOTE:</b><ul>");
			
			if (!unmappedIssueStates.isEmpty()) { 
				feedback.append("<li> Unmapped YouTrack issue states (using OneDev initial state): " 
						+ HtmlEscape.escapeHtml5(unmappedIssueStates.toString()));
			}
			if (!unmappedIssueFields.isEmpty()) { 
				feedback.append("<li> Unmapped YouTrack issue fields (mentioned as extra info in issue description): " 
						+ HtmlEscape.escapeHtml5(unmappedIssueFields.toString()));
			}
			if (!mismatchedIssueFields.isEmpty()) { 
				feedback.append("<li> YouTrack issue fields mapped to wrong type of OneDev issue field (mentioned as extra info in issue description):");
				feedback.append("<ul>");
				for (Map.Entry<String, String> entry: mismatchedIssueFields.entrySet()) { 
					feedback.append("<li>")
							.append(HtmlEscape.escapeHtml5(entry.getKey()))
							.append(" : ")
							.append(HtmlEscape.escapeHtml5(entry.getValue()));
				}
				feedback.append("</ul>");
			}
			if (!unmappedIssueTags.isEmpty()) { 
				feedback.append("<li> YouTrack issue tags not mapped to OneDev custom field (mentioned as extra info in issue description): " 
						+ HtmlEscape.escapeHtml5(unmappedIssueTags.toString()));
			}
			if (!unmappedAccounts.isEmpty()) {
				feedback.append("<li> YouTrack logins without email or email can not be mapped to OneDev account: " 
						+ HtmlEscape.escapeHtml5(unmappedAccounts.toString()));
			}
			if (!tooLargeAttachments.isEmpty()) {
				feedback.append("<li> Too large attachments: " 
						+ HtmlEscape.escapeHtml5(tooLargeAttachments.toString()));
			}
			
			if (hasNotes)
				feedback.append("</ul>");
			
			return feedback.toString();
		} catch (Exception e) {
			for (Long projectId: projectIds)
				OneDev.getInstance(StorageManager.class).deleteProjectDir(projectId);
			throw new RuntimeException(e);
		} finally {
			client.close();
		}
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private List<JsonNode> list(Client client, YouTrackImportSource importSource, 
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
	
	private void list(Client client, YouTrackImportSource importSource, String apiPath, 
			PageDataConsumer pageDataConsumer, SimpleLogger logger) {
		URI uri;
		try {
			uri = new URIBuilder(importSource.getApiEndpoint(apiPath))
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
	
	private JsonNode get(Client client, String apiEndpoint, SimpleLogger logger) {
		WebTarget target = client.target(apiEndpoint);
		Invocation.Builder builder =  target.request();
		while (true) {
			try (Response response = builder.get()) {
				String errorMessage = checkStatus(response);
				if (errorMessage != null)
					throw new ExplicitException(errorMessage);
				else
					return response.readEntity(JsonNode.class);
			}
		}
	}
	
	@Nullable
	public static String checkStatus(Response response) {
		int status = response.getStatus();
		if (status != 200) {
			String errorMessage = response.readEntity(String.class);
			if (StringUtils.isNotBlank(errorMessage)) {
				return String.format("Http request failed (status code: %d, error message: %s)", 
						status, errorMessage);
			} else {
				return String.format("Http request failed (status code: %d)", status);
			}
		} else {
			return null;
		}
	}
	
	private Client newClient(YouTrackImportSource importSource) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(importSource.getUserName(), importSource.getPassword()));
		return client;
	}

	private static interface PageDataConsumer {
		
		void consume(List<JsonNode> pageData) throws InterruptedException;
		
	}	
	
}