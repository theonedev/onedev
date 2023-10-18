package io.onedev.server.plugin.imports.jiracloud;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.ReferenceMigrator;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.issue.IssuesImported;
import io.onedev.server.model.*;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Editable
@ClassValidating
public class ImportServer implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ImportServer.class);
	
	private static final int PER_PAGE = 50;
	
	private static final String PROP_API_URL = "apiUrl";
	
	private String apiUrl;
	
	private String accountEmail;
	
	private String apiToken;

	@Editable(order=5, description="API url of your JIRA cloud instance, for instance, <tt>https://your-domain.atlassian.net/rest/api/3</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Editable(order=10)
	@Email
	@NotEmpty
	public String getAccountEmail() {
		return accountEmail;
	}

	public void setAccountEmail(String accountEmail) {
		this.accountEmail = accountEmail;
	}

	@Editable(order=100)
	@Password
	@NotEmpty
	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}
	
	private String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}
	
	List<String> listProjects() {
		List<String> projects = new ArrayList<>();
		
		Client client = newClient();
		try {
			String apiEndpoint = getApiEndpoint("/project/search");
			for (JsonNode projectNode: list(client, apiEndpoint, new TaskLogger() {

				@Override
				public void log(String message, String sessionId) {
					logger.info(message);
				}
				
			})) {
				projects.add(projectNode.get("name").asText());
			}
		} finally {
			client.close();
		}
		
		Collections.sort(projects);
		return projects;
	}

	private Client newClient() {
		Client client = ClientBuilder.newClient();
		client.property(ClientProperties.FOLLOW_REDIRECTS, true);
		client.register(HttpAuthenticationFeature.basic(getAccountEmail(), getApiToken()));
		return client;
	}
	
	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Nullable
	private Long getUserId(Map<String, Optional<Long>> userIds, JsonNode userNode, TaskLogger logger) {
		String accountId = userNode.get("accountId").asText();
		Optional<Long> userIdOpt = userIds.get(accountId);
		if (userIdOpt == null) {
			String displayName = null;
			if (userNode.hasNonNull("displayName"))
				displayName = userNode.get("displayName").asText(null);
			if (displayName != null)
				userIdOpt = Optional.ofNullable(User.idOf(OneDev.getInstance(UserManager.class).findByFullName(displayName)));
			else
				userIdOpt = Optional.empty();
			userIds.put(accountId, userIdOpt);
		}
		return userIdOpt.orElse(null);
	}
	
	private String getMarkdown(JsonNode contentNode, boolean escape) {
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
	
	TaskResult importProjects(ImportProjects projects, ImportOption option, boolean dryRun, TaskLogger logger) {
		Client client = newClient();
		try {
			Map<String, Optional<Long>> userIds = new HashMap<>();
			Map<String, JsonNode> projectNodes = getProjectNodes(logger);
			ImportResult result = new ImportResult();
			for (var jiraProject: projects.getImportProjects()) {
				OneDev.getInstance(TransactionManager.class).run(() -> {
					String oneDevProjectPath;
					if (projects.getParentOneDevProject() != null)
						oneDevProjectPath = projects.getParentOneDevProject() + "/" + jiraProject;
					else
						oneDevProjectPath = jiraProject;

					logger.log("Importing from '" + jiraProject + "' to '" + oneDevProjectPath + "'...");

					ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
					Project project = projectManager.setup(oneDevProjectPath);

					if (!project.isNew() && !SecurityUtils.canManage(project)) {
						throw new UnauthorizedException("Import target already exists. " +
								"You need to have project management privilege over it");
					}

					JsonNode projectNode = projectNodes.get(jiraProject);
					if (projectNode == null)
						throw new ExplicitException("Unable to find project: " + jiraProject);

					String apiEndpoint = getApiEndpoint("/project/" + projectNode.get("id").asText());

					// Get more detail project information
					projectNode = JerseyUtils.get(client, apiEndpoint, logger);

					project.setDescription(projectNode.get("description").asText(null));

					if (!dryRun && project.isNew())
						projectManager.create(project);

					logger.log("Importing issues...");
					ImportResult currentResult = importIssues(projectNode, project, option, userIds, dryRun, logger);
					result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
					result.errorAttachments.addAll(currentResult.errorAttachments);
					result.tooLargeAttachments.addAll(currentResult.tooLargeAttachments);
					result.unmappedIssuePriorities.addAll(currentResult.unmappedIssuePriorities);
					result.unmappedIssueStatuses.addAll(currentResult.unmappedIssueStatuses);
					result.unmappedIssueTypes.addAll(currentResult.unmappedIssueTypes);
				});
			}
			
			return new TaskResult(true, new HtmlMessgae(result.toHtml("Projects imported successfully")));
		} finally {
			client.close();
		}	
	}
	
	TaskResult importIssues(Long projectId, String jiraProject, ImportOption option, boolean dryRun, TaskLogger logger) {
		Map<String, JsonNode> projectNodes = getProjectNodes(logger);
		Client client = newClient();
		try {
			JsonNode projectNode = projectNodes.get(jiraProject);
			if (projectNode == null)
				throw new ExplicitException("Unable to find project: " + jiraProject);
			return OneDev.getInstance(TransactionManager.class).call(() -> {
				var project = OneDev.getInstance(ProjectManager.class).load(projectId);
				Map<String, Optional<Long>> userIds = new HashMap<>();
				ImportResult result = importIssues(projectNode, project, option, userIds, dryRun, logger);
				return new TaskResult(true, new HtmlMessgae(result.toHtml("Issues imported successfully")));
			});
		} finally {
			client.close();
		}
	}
	
	private ImportResult importIssues(JsonNode jiraProject, Project oneDevProject, ImportOption option, 
			Map<String, Optional<Long>> userIds, boolean dryRun, TaskLogger logger) {
		IssueManager issueManager = OneDev.getInstance(IssueManager.class);
		Client client = newClient();
		try {
			Set<String> unmappedIssueStatuses = new HashSet<>();
			Set<String> unmappedIssueTypes = new HashSet<>();
			Set<String> unmappedIssuePriorities = new HashSet<>();
			Set<String> nonExistentLogins = new HashSet<>();
			Set<String> tooLargeAttachments = new LinkedHashSet<>();
			Set<String> errorAttachments = new HashSet<>();
			
			Map<String, Pair<FieldSpec, String>> typeMappings = new HashMap<>();
			
			for (IssueTypeMapping mapping: option.getIssueTypeMappings()) {
				String oneDevFieldName = StringUtils.substringBefore(mapping.getOneDevIssueField(), "::");
				String oneDevFieldValue = StringUtils.substringAfter(mapping.getOneDevIssueField(), "::");
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(oneDevFieldName);
				if (fieldSpec == null)
					throw new ExplicitException("No field spec found: " + oneDevFieldName);
				typeMappings.put(mapping.getJiraIssueType(), new Pair<>(fieldSpec, oneDevFieldValue));
			}
			
			Map<String, Pair<FieldSpec, String>> priorityMappings = new HashMap<>();
			
			for (IssuePriorityMapping mapping: option.getIssuePriorityMappings()) {
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
			for (IssueStatusMapping mapping: option.getIssueStatusMappings())
				statusMappings.put(mapping.getJiraIssueStatus(), mapping.getOneDevIssueState());
			
			Map<String, String> untranslatedStatusNames = new HashMap<>();
			String apiEndpoint = getApiEndpoint("/status");
			for (JsonNode statusNode: list(client, apiEndpoint, logger)) {
				String untranslated = statusNode.get("untranslatedName").asText();
				untranslatedStatusNames.put(statusNode.get("id").asText(), untranslated);
			}
			
			Map<String, String> untranslatedTypeNames = new HashMap<>();
			apiEndpoint = getApiEndpoint("/issuetype/project?projectId=" + jiraProject.get("id").asText());
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
						String apiEndpoint = getApiEndpoint("/issue/" + issueKey);
						issueNode = JerseyUtils.get(client, apiEndpoint, logger);
						
						Map<String, String> extraIssueInfo = new LinkedHashMap<>();
						
						Issue issue = new Issue();
						issue.setProject(oneDevProject);
						
						issue.setNumberScope(oneDevProject.getForkRoot());

						Long newNumber;
						Long oldNumber = Long.valueOf(StringUtils.substringAfterLast(issueNode.get("key").asText(), "-"));
						if (dryRun || (issueManager.find(oneDevProject, oldNumber) == null && !issueNumberMappings.containsValue(oldNumber))) 
							newNumber = oldNumber;
						else
							newNumber = issueManager.getNextNumber(oneDevProject);
						
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
								typeField.setName(mapped.getLeft().getName());
								typeField.setType(InputSpec.ENUMERATION);
								typeField.setValue(mapped.getRight());
								typeField.setOrdinal(mapped.getLeft().getOrdinal(mapped.getRight()));
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
							priorityField.setName(mapped.getLeft().getName());
							priorityField.setType(InputSpec.ENUMERATION);
							priorityField.setValue(mapped.getRight());
							priorityField.setOrdinal(mapped.getLeft().getOrdinal(mapped.getRight()));
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
						
						var userManager = OneDev.getInstance(UserManager.class);
						JsonNode authorNode = fieldsNode.get("reporter");
						if (authorNode == null)
							authorNode = fieldsNode.get("creator");
						if (authorNode != null) {
							Long userId = getUserId(userIds, authorNode, logger);
							if (userId != null) {
								issue.setSubmitter(userManager.load(userId));
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
							assigneeField.setName(option.getAssigneeIssueField());
							assigneeField.setType(InputSpec.USER);
							
							Long userId = getUserId(userIds, assigneeNode, logger);
							if (userId != null) { 
								assigneeField.setValue(userManager.load(userId).getName());
								issue.getFields().add(assigneeField);
							} else {
								nonExistentLogins.add(assigneeNode.get("displayName").asText());
							}
						}
						
						issue.setSubmitDate(ISODateTimeFormat.dateTime()
								.parseDateTime(fieldsNode.get("created").asText())
								.toDate());
						
						LastActivity lastActivity = new LastActivity();
						lastActivity.setDescription("Opened");
						lastActivity.setDate(issue.getSubmitDate());
						lastActivity.setUser(issue.getSubmitter());
						issue.setLastActivity(lastActivity);

						if (option.getDueDateIssueField() != null) {
							String dueDate = fieldsNode.get("duedate").asText(null);
							if (dueDate != null) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(option.getDueDateIssueField());
								issueField.setType(InputSpec.DATE);
								issueField.setValue(dueDate);
								issue.getFields().add(issueField);
							}
						}
						
						if (option.getTimeEstimateIssueField() != null) {
							String timeEstimate = fieldsNode.get("timeestimate").asText(null);
							if (timeEstimate != null) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(option.getTimeEstimateIssueField());
								issueField.setType(InputSpec.WORKING_PERIOD);
								issueField.setValue(DateUtils.formatWorkingPeriod(Integer.valueOf(timeEstimate)/60));
								issue.getFields().add(issueField);
							}
						}
						if (option.getTimeSpentIssueField() != null) {
							String timeSpent = fieldsNode.get("timespent").asText(null);
							if (timeSpent != null) {
								IssueField issueField = new IssueField();
								issueField.setIssue(issue);
								issueField.setName(option.getTimeSpentIssueField());
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
										Long userId = getUserId(userIds, authorNode, logger);
										if (userId != null) {
											comment.setUser(userManager.load(userId));
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
												AttachmentManager attachmentManager = OneDev.getInstance(AttachmentManager.class);
												String oneDevAttachmentName = attachmentManager.saveAttachment(
														oneDevProject.getId(), issue.getUUID(), attachmentName, is);
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
			apiEndpoint = getApiEndpoint("/search?jql=" + jql);
			list(client, apiEndpoint, pageDataConsumer, logger);

			if (!dryRun) {
				ReferenceMigrator migrator = new ReferenceMigrator(Issue.class, issueNumberMappings);
				String jiraProjectKey = jiraProject.get("key").asText();
				Dao dao = OneDev.getInstance(Dao.class);
				for (Issue issue: issues) {
					if (issue.getDescription() != null) 
						issue.setDescription(migrator.migratePrefixed(issue.getDescription(), jiraProjectKey + "-"));
					
					dao.persist(issue);
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
			
			if (!dryRun && !issues.isEmpty())
				OneDev.getInstance(ListenerRegistry.class).post(new IssuesImported(oneDevProject, issues));
			
			return result;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} finally {
			if (!dryRun)
				issueManager.resetNextNumber(oneDevProject);
			client.close();
		}
	}
	
	ImportOption buildImportOption(Collection<String> projectNames) {
		ImportOption option = new ImportOption();
		
		TaskLogger taskLogger = new TaskLogger() {

			@Override
			public void log(String message, String sessionId) {
				logger.info(message);
			}
			
		};
		Map<String, JsonNode> projectNodes = getProjectNodes(taskLogger);
		
		Client client = newClient();
		try {
			String apiEndpoint = getApiEndpoint("/status");
			List<JsonNode> statusNodes = list(client, apiEndpoint, taskLogger);
			
			Set<String> issueTypes = new LinkedHashSet<>();
			Set<String> issueStatuses = new LinkedHashSet<>();
			for (String projectName: projectNames) {
				JsonNode projectNode = projectNodes.get(projectName);
				if (projectNode == null)
					throw new ExplicitException("Unable to find project: " + projectName);
				apiEndpoint = getApiEndpoint("/issuetype/project?projectId=" + projectNode.get("id").asText()); 
				for (JsonNode issueTypeNode: list(client, apiEndpoint, taskLogger))  
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
				option.getIssueTypeMappings().add(mapping);
			}
			for (String issueStatus: issueStatuses) {
				IssueStatusMapping mapping = new IssueStatusMapping();
				mapping.setJiraIssueStatus(issueStatus);
				option.getIssueStatusMappings().add(mapping);
			}
			apiEndpoint = getApiEndpoint("/priority");
			for (JsonNode priorityNode: list(client, apiEndpoint, taskLogger)) {
				IssuePriorityMapping mapping = new IssuePriorityMapping();
				mapping.setJiraIssuePriority(priorityNode.get("name").asText());
				option.getIssuePriorityMappings().add(mapping);
			}
		} finally {
			client.close();
		}
		return option;
	}
	
	private Map<String, JsonNode> getProjectNodes(TaskLogger logger) {
		Map<String, JsonNode> projectNodes = new LinkedHashMap<>();
		
		Client client = newClient();
		try {
			for (JsonNode projectNode: list(client, getApiEndpoint("/project/search"), logger)) 
				projectNodes.put(projectNode.get("name").asText(), projectNode);
		} finally {
			client.close();
		}
		return projectNodes;
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
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Client client = ClientBuilder.newClient();
		client.register(HttpAuthenticationFeature.basic(getAccountEmail(), getApiToken()));
		try {
			String apiEndpoint = getApiEndpoint("/myself");
			WebTarget target = client.target(apiEndpoint);
			Invocation.Builder builder =  target.request();
			builder.header("Accept", MediaType.APPLICATION_JSON);
			try (Response response = builder.get()) {
				if (response.getStatus() == 401) {
					context.disableDefaultConstraintViolation();
					String errorMessage = "Authentication failed";
					context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
					return false;
				} else if (!response.getMediaType().toString().startsWith("application/json") 
						|| response.getStatus() == 404) {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate("This does not seem like a JIRA api url")
							.addPropertyNode(PROP_API_URL).addConstraintViolation();
					return false;
				} else {
					String errorMessage = JerseyUtils.checkStatus(apiEndpoint, response);
					if (errorMessage != null) {
						context.disableDefaultConstraintViolation();
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
			context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		} finally {
			client.close();
		}
		return true;
	}
	
}
