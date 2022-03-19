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
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.JsonNode;

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
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.JerseyUtils.PageDataConsumer;
import io.onedev.server.util.Pair;

public class ImportUtils {

	static final String NAME = "GitHub";

	static final int PER_PAGE = 50;
	
	static IssueImportOption buildImportOption(ImportServer server, Collection<String> gitHubRepos, TaskLogger logger) {
		IssueImportOption importOption = new IssueImportOption();
		Client client = server.newClient();
		try {
			Set<String> labels = new LinkedHashSet<>();
			for (String each: gitHubRepos) {
				String apiEndpoint = server.getApiEndpoint("/repos/" + each + "/labels"); 
				for (JsonNode labelNode: list(client, apiEndpoint, logger)) 
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
	static User getUser(Client client, ImportServer importSource, 
			Map<String, Optional<User>> users, String login, TaskLogger logger) {
		Optional<User> userOpt = users.get(login);
		if (userOpt == null) {
			String apiEndpoint = importSource.getApiEndpoint("/users/" + login);
			String email = get(client, apiEndpoint, logger).get("email").asText(null);
			if (email != null) 
				userOpt = Optional.ofNullable(OneDev.getInstance(UserManager.class).findByVerifiedEmailAddress(email));
			else 
				userOpt = Optional.empty();
			users.put(login, userOpt);
		}
		return userOpt.orElse(null);
	}
	
	static ImportResult importIssues(ImportServer server, String gitHubRepo, Project oneDevProject,
			boolean useExistingIssueNumbers, IssueImportOption importOption, Map<String, Optional<User>> users, 
			boolean dryRun, TaskLogger logger) {
		Client client = server.newClient();
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

						Map<String, String> extraIssueInfo = new LinkedHashMap<>();
						
						Issue issue = new Issue();
						issue.setProject(oneDevProject);
						issue.setTitle(issueNode.get("title").asText());
						issue.setDescription(issueNode.get("body").asText(null));
						issue.setNumberScope(oneDevProject.getForkRoot());

						Long oldNumber = issueNode.get("number").asLong();
						Long newNumber;
						if (dryRun || useExistingIssueNumbers)
							newNumber = oldNumber;
						else
							newNumber = OneDev.getInstance(IssueManager.class).getNextNumber(oneDevProject);
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
						
						String login = issueNode.get("user").get("login").asText(null);
						User user = getUser(client, server, users, login, logger);
						if (user != null) {
							issue.setSubmitter(user);
						} else {
							issue.setSubmitter(OneDev.getInstance(UserManager.class).getUnknown());
							nonExistentLogins.add(login);
						}
						
						issue.setSubmitDate(ISODateTimeFormat.dateTimeNoMillis()
								.parseDateTime(issueNode.get("created_at").asText())
								.toDate());
						
						LastUpdate lastUpdate = new LastUpdate();
						lastUpdate.setActivity("Opened");
						lastUpdate.setDate(issue.getSubmitDate());
						lastUpdate.setUser(issue.getSubmitter());
						issue.setLastUpdate(lastUpdate);

						for (JsonNode assigneeNode: issueNode.get("assignees")) {
							IssueField assigneeField = new IssueField();
							assigneeField.setIssue(issue);
							assigneeField.setName(importOption.getAssigneesIssueField());
							assigneeField.setType(InputSpec.USER);
							
							login = assigneeNode.get("login").asText();
							user = getUser(client, server, users, login, logger);
							if (user != null) { 
								assigneeField.setValue(user.getName());
								issue.getFields().add(assigneeField);
							} else {
								nonExistentLogins.add(login);
							}
						}

						String apiEndpoint = server.getApiEndpoint("/repos/" + gitHubRepo 
								+ "/issues/" + oldNumber + "/comments");
						for (JsonNode commentNode: list(client, apiEndpoint, logger)) {
							IssueComment comment = new IssueComment();
							comment.setIssue(issue);
							comment.setContent(commentNode.get("body").asText(null));
							comment.setDate(ISODateTimeFormat.dateTimeNoMillis()
									.parseDateTime(commentNode.get("created_at").asText())
									.toDate());
							
							login = commentNode.get("user").get("login").asText();
							user = getUser(client, server, users, login, logger);
							if (user != null) {
								comment.setUser(user);
							} else {
								comment.setUser(OneDev.getInstance(UserManager.class).getUnknown());
								nonExistentLogins.add(login);
							}

							issue.getComments().add(comment);
						}
						
						issue.setCommentCount(issue.getComments().size());
						
						apiEndpoint = server.getApiEndpoint("/repos/" + gitHubRepo 
								+ "/issues/" + oldNumber + "/labels");
						List<String> currentUnmappedLabels = new ArrayList<>();
						for (JsonNode labelNode: list(client, apiEndpoint, logger)) {
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
					}
					logger.log("Imported " + numOfImportedIssues.addAndGet(pageData.size()) + " issues");
				}
				
			};

			String apiEndpoint = server.getApiEndpoint("/repos/" + gitHubRepo + "/issues?state=all&direction=asc");
			list(client, apiEndpoint, pageDataConsumer, logger);

			if (!dryRun) {
				ReferenceMigrator migrator = new ReferenceMigrator(Issue.class, issueNumberMappings);
				Dao dao = OneDev.getInstance(Dao.class);
				for (Issue issue: issues) {
					if (issue.getDescription() != null) 
						issue.setDescription(migrator.migratePrefixed(issue.getDescription(), "#"));
					
					OneDev.getInstance(IssueManager.class).save(issue);
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
			
			if (numOfImportedIssues.get() != 0)
				result.issuesImported = true;
			
			return result;
		} finally {
			client.close();
		}
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
	
	static void list(Client client, String apiEndpoint, PageDataConsumer pageDataConsumer, 
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
	
	static JsonNode get(Client client, String apiEndpoint, TaskLogger logger) {
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
	
}