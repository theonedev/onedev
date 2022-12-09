package io.onedev.server.plugin.imports.youtrack;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.client.utils.URIBuilder;
import org.apache.wicket.MetaDataKey;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.ProjectManager;
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
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
@ClassValidating
public class ImportServer implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ImportServer.class);
	
	private static final int PER_PAGE = 50;
	
	static final MetaDataKey<ImportServer> META_DATA_KEY = new MetaDataKey<ImportServer>() {

		private static final long serialVersionUID = 1L;
		
	};
	
	protected static final String PROP_API_URL = "apiUrl";
	
	private String apiUrl;
	
	private String userName;
	
	private String password;
	
	@Editable(order=10, name="YouTrack API URL", description="Specify url of YouTrack API. For instance <tt>http://localhost:8080/api</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	@Editable(order=200, name="YouTrack Login Name", description="Specify YouTrack login name. This account should have permission to:"
			+ "<ul>"
			+ "<li>Read full information and issues of the projects you want to import"
			+ "<li>Read issue tags"
			+ "<li>Read user basic information"
			+ "</ul>")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=300, name="YouTrack Password or Access Token", description="Specify YouTrack password or access token for above user")
	@Password
	@NotEmpty
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}
	
	List<String> listProjects() {
		List<String> projects = new ArrayList<>();
		
		Client client = newClient();
		try {
			TaskLogger logger = new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					ImportServer.logger.info(message);
				}
				
			};
			String apiEndpoint = getApiEndpoint("/admin/projects?fields=name");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) 
				projects.add(projectNode.get("name").asText());
		} finally {
			client.close();
		}
		
		Collections.sort(projects);
		return projects;
	}
	
	private Client newClient() {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(getUserName(), getPassword()));
		return client;
	}

	ImportOption buildImportOption(ImportProjects projects) {
		Client client = newClient();
		try {
			List<JsonNode> projectNodes = new ArrayList<>();
			String apiEndpoint = getApiEndpoint("/admin/projects?fields=id,name,customFields(field(name),bundle(values(name)))");
			Set<String> projectNames = projects.getProjectMappings().stream()
					.map(it->it.getYouTrackProject()).collect(Collectors.toSet()); 
			for (JsonNode projectNode: list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			})) {
				if (projectNames.contains(projectNode.get("name").asText())) 
					projectNodes.add(projectNode);
			}
			
			return buildImportOption(projectNodes);
		} finally {
			client.close();
		}
	}
	
	ImportOption buildImportOption(String project) {
		Client client = newClient();
		try {
			String apiEndpoint = getApiEndpoint("/admin/projects?fields=id,name,customFields(field(name),bundle(values(name)))");
			for (JsonNode projectNode: list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			})) {
				if (project.equals(projectNode.get("name").asText())) {  
					List<JsonNode> projectNodes = new ArrayList<>();
					projectNodes.add(projectNode);
					return buildImportOption(projectNodes);
				}
			}
			throw new ExplicitException("Unable to find YouTrack project: " + project);
		} finally {
			client.close();
		}
	}
	
	ImportOption buildImportOption(Collection<JsonNode> projectNodes) {
		ImportOption option = new ImportOption();
		Client client = newClient();
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
				option.getIssueStateMappings().add(mapping);
			}
			
			for (String youTrackIssueField: youTrackIssueFields) {
				IssueFieldMapping mapping = new IssueFieldMapping();
				mapping.setYouTrackIssueField(youTrackIssueField);
				option.getIssueFieldMappings().add(mapping);
			}

			TaskLogger taskLogger = new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			};
			String apiEndpoint = getApiEndpoint("/issueTags?fields=name");
			for (JsonNode tagNode: list(client, apiEndpoint, taskLogger)) {
				IssueTagMapping mapping = new IssueTagMapping();
				mapping.setYouTrackIssueTag(tagNode.get("name").asText());
				option.getIssueTagMappings().add(mapping);
			}
			
			apiEndpoint = getApiEndpoint("/issueLinkTypes?fields=name");
			for (JsonNode tagNode: list(client, apiEndpoint, taskLogger)) {
				IssueLinkMapping mapping = new IssueLinkMapping();
				mapping.setYouTrackIssueLink(tagNode.get("name").asText());
				option.getIssueLinkMappings().add(mapping);
			}
			
		} finally {
			client.close();
		}
		return option;
	}
	
	private ImportResult importIssues(String youTrackProjectId, Project oneDevProject,  
			ImportOption option, boolean dryRun, TaskLogger logger) {
		IssueManager issueManager = OneDev.getInstance(IssueManager.class);
		Client client = newClient();
		try {
			String apiEndpoint = getApiEndpoint("/admin/projects/" + youTrackProjectId + "?fields=shortName");
			String youTrackProjectShortName = JerseyUtils.get(client, apiEndpoint, logger).get("shortName").asText();
			
			Map<String, String> stateMappings = new HashMap<>();
			Map<String, Pair<FieldSpec, String>> fieldMappings = new HashMap<>(); 
			Map<String, Pair<FieldSpec, String>> tagMappings = new HashMap<>();
			Map<String, LinkSpec> linkMappings = new HashMap<>();
			
			Map<Long, Long> issueNumberMappings = new HashMap<>();
			Map<Long, Issue> issueMappings = new HashMap<>();
			
			for (IssueStateMapping mapping: option.getIssueStateMappings())
				stateMappings.put(mapping.getYouTrackIssueState(), mapping.getOneDevIssueState());
			
			for (IssueFieldMapping mapping: option.getIssueFieldMappings()) {
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
			for (IssueTagMapping mapping: option.getIssueTagMappings()) {
				String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
				String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				tagMappings.put(mapping.getYouTrackIssueTag(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			for (IssueLinkMapping mapping: option.getIssueLinkMappings()) {
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

								String endpoint = getApiEndpoint(attachmentUrl);
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
										AttachmentManager attachmentManager = OneDev.getInstance(AttachmentManager.class);
										String oneDevAttachmentName = attachmentManager.saveAttachment(
												oneDevProject.getId(), issueUUID, attachmentName, is);
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
						
						Long newNumber;
						Long oldNumber = issueNode.get("numberInProject").asLong();
						if (dryRun || (issueManager.find(oneDevProject, oldNumber) == null && !issueNumberMappings.containsValue(oldNumber))) 
							newNumber = oldNumber;
						else
							newNumber = issueManager.getNextNumber(oneDevProject);
						
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
			
			apiEndpoint = getApiEndpoint("/admin/projects/" + youTrackProjectId + "/issues?fields=" + fields);
			list(client, apiEndpoint, pageDataConsumer, logger);

			if (!dryRun) {
				ReferenceMigrator migrator = new ReferenceMigrator(Issue.class, issueNumberMappings);
				Dao dao = OneDev.getInstance(Dao.class);
				for (Issue issue: issues) {
					if (issue.getDescription() != null) 
						issue.setDescription(migrator.migratePrefixed(issue.getDescription(), youTrackProjectShortName + "-"));
					
					issueManager.save(issue);
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
			if (!dryRun)
				issueManager.resetNextNumber(oneDevProject);
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
	
	String importProjects(ImportProjects projects, ImportOption option, boolean dryRun, TaskLogger logger) {
		Map<String, String> youTrackProjectIds = new HashMap<>();
		Map<String, String> youTrackProjectDescriptions = new HashMap<>();
		Client client = newClient();
		try {
			String apiEndpoint = getApiEndpoint("/admin/projects?fields=id,name,description");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) { 
				String projectName = projectNode.get("name").asText();
				youTrackProjectIds.put(projectName, projectNode.get("id").asText());
				youTrackProjectDescriptions.put(projectName, projectNode.get("description").asText(null));
			}
			
			ImportResult result = new ImportResult();
			
			for (ProjectMapping projectMapping: projects.getProjectMappings()) {
				logger.log("Importing project '" + projectMapping.getYouTrackProject() + "'...");
				
				String youTrackProjectId = youTrackProjectIds.get(projectMapping.getYouTrackProject());
				if (youTrackProjectId == null)
					throw new ExplicitException("Unable to find YouTrack project: " + projectMapping.getYouTrackProject());
				
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);				
				Project project = projectManager.setup(projectMapping.getOneDevProject());
				project.setDescription(youTrackProjectDescriptions.get(projectMapping.getYouTrackProject()));
				project.setIssueManagement(true);
				
		       	if (!dryRun && project.isNew()) 
					projectManager.create(project);

		       	ImportResult currentResult = importIssues(youTrackProjectId, project, 
		       			option, dryRun, logger);
		       	result.mismatchedIssueFields.putAll(currentResult.mismatchedIssueFields);
		       	result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
		       	result.tooLargeAttachments.addAll(currentResult.tooLargeAttachments);
		       	result.unmappedIssueFields.addAll(currentResult.unmappedIssueFields);
		       	result.unmappedIssueStates.addAll(currentResult.unmappedIssueStates);
		       	result.unmappedIssueLinks.addAll(currentResult.unmappedIssueLinks);
		       	result.unmappedIssueTags.addAll(currentResult.unmappedIssueTags);
			}		       	

			return result.toHtml("Projects imported successfully");
		} finally {
			client.close();
		}			
	}	
	
	private void list(Client client, String apiEndpoint, PageDataConsumer pageDataConsumer, TaskLogger logger) {
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
	
	String importIssues(Project project, String youTrackProject, ImportOption option, 
			boolean dryRun, TaskLogger logger) {
		logger.log("Importing issues from '" + youTrackProject + "'...");
		Client client = newClient();
		try {
			String apiEndpoint = getApiEndpoint("/admin/projects?fields=id,name");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) {
				if (youTrackProject.equals(projectNode.get("name").asText())) { 
					ImportResult result = importIssues(projectNode.get("id").asText(), 
							project, option, dryRun, logger);
					return result.toHtml("Issues imported successfully");
				}
			}
			throw new ExplicitException("Unable to find YouTrack project: " + youTrackProject);
		} finally {
			client.close();
		}
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		try {
			client.register(HttpAuthenticationFeature.basic(getUserName(), getPassword()));
			String apiEndpoint = getApiEndpoint("/users/me?fields=guest");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			try (Response response = builder.get()) {
				if (!response.getMediaType().toString().startsWith("application/json") 
						|| response.getStatus() == 404) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("This does not seem like a YouTrack api url")
							.addPropertyNode(PROP_API_URL).addConstraintViolation();
					return false;
				} 				
				String errorMessage = JerseyUtils.checkStatus(apiEndpoint, response);
				if (errorMessage != null) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate(errorMessage)
							.addPropertyNode(PROP_API_URL).addConstraintViolation();
					return false;
				} else {
					JsonNode userNode = response.readEntity(JsonNode.class);
					if (userNode.get("guest").asBoolean()) {
						context.disableDefaultConstraintViolation();
						errorMessage = "Authentication failed";
						context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
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
