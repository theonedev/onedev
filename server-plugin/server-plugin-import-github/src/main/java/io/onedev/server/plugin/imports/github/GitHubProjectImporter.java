package io.onedev.server.plugin.imports.github;

import static io.onedev.server.plugin.imports.github.GitHubImportUtils.NAME;
import static io.onedev.server.plugin.imports.github.GitHubImportUtils.buildImportOption;
import static io.onedev.server.plugin.imports.github.GitHubImportUtils.get;
import static io.onedev.server.plugin.imports.github.GitHubImportUtils.importIssues;
import static io.onedev.server.plugin.imports.github.GitHubImportUtils.list;
import static io.onedev.server.plugin.imports.github.GitHubImportUtils.newClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;

import org.apache.http.client.utils.URIBuilder;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.page.project.imports.ProjectImporter;

public class GitHubProjectImporter extends ProjectImporter<GitHubProjectImportSource, GitHubProjectImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public GitHubProjectImportOption getImportOption(GitHubProjectImportSource importSource, SimpleLogger logger) {
		GitHubProjectImportOption importOption = new GitHubProjectImportOption();
		if (importSource.isPrepopulateImportOptions()) {
			Client client = newClient(importSource);
			try {
				String apiEndpoint = importSource.getApiEndpoint("/user/repos");
				for (JsonNode repoNode: list(client, apiEndpoint, logger)) {
					String repoName = repoNode.get("name").asText();
					String ownerName = repoNode.get("owner").get("login").asText();
					ProjectMapping projectMapping = new ProjectMapping();
					projectMapping.setGitHubRepo(ownerName + "/" + repoName);
					projectMapping.setOneDevProject(ownerName + "-" + repoName);
					importOption.getProjectMappings().add(projectMapping);
				}					
				GitHubIssueImportOption issueImportOption = buildImportOption(importSource, null, logger);
				importOption.setAssigneesIssueField(issueImportOption.getAssigneesIssueField());
				importOption.setClosedIssueState(issueImportOption.getClosedIssueState());
				importOption.setIssueLabelMappings(issueImportOption.getIssueLabelMappings());
			} finally {
				client.close();
			}
		}
		return importOption;
	}
	
	@Nullable
	private User getUser(Client client, GitHubProjectImportSource importSource, 
			Map<String, Optional<User>> users, String login, SimpleLogger logger) {
		Optional<User> userOpt = users.get(login);
		if (userOpt == null) {
			String apiEndpoint = importSource.getApiEndpoint("/users/" + login);
			String email = get(client, apiEndpoint, logger).get("email").asText(null);
			if (email != null) 
				userOpt = Optional.ofNullable(OneDev.getInstance(UserManager.class).findByEmail(email));
			else 
				userOpt = Optional.empty();
			users.put(login, userOpt);
		}
		return userOpt.orElse(null);
	}
	
	@Override
	public String doImport(GitHubProjectImportSource importSource, GitHubProjectImportOption importOption, 
			boolean dryRun, SimpleLogger logger) {
		Collection<Long> projectIds = new ArrayList<>();
		Client client = newClient(importSource);
		try {
			Map<String, Optional<User>> users = new HashMap<>();
			GitHubImportResult result = new GitHubImportResult();
			for (ProjectMapping projectMapping: importOption.getProjectMappings()) {
				logger.log("Cloning code from repository " + projectMapping.getGitHubRepo() + "...");
				
				String apiEndpoint = importSource.getApiEndpoint("/repos/" + projectMapping.getGitHubRepo());
				JsonNode repoNode = get(client, apiEndpoint, logger);
				Project project = new Project();
				project.setName(projectMapping.getOneDevProject());
				project.setDescription(repoNode.get("description").asText(null));
				project.setIssueManagementEnabled(repoNode.get("has_issues").asBoolean());
				
				boolean isPrivate = repoNode.get("private").asBoolean();
				if (!isPrivate && importOption.getPublicRole() != null)
					project.setDefaultRole(importOption.getPublicRole());
				
				URIBuilder builder = new URIBuilder(repoNode.get("clone_url").asText());
				if (isPrivate)
					builder.setUserInfo("git", importSource.getAccessToken());
				
				if (!dryRun) {
					OneDev.getInstance(ProjectManager.class).clone(project, builder.build().toString());
					projectIds.add(project.getId());
				}

				if (projectMapping.isImportIssues()) {
					Map<Long, Milestone> milestones = new HashMap<>();
					logger.log("Importing milestones from repository " + projectMapping.getGitHubRepo() + "...");
					apiEndpoint = importSource.getApiEndpoint("/repos/" + projectMapping.getGitHubRepo() + "/milestones?state=all");
					for (JsonNode milestoneNode: list(client, apiEndpoint, logger)) {
						Milestone milestone = new Milestone();
						milestone.setName(milestoneNode.get("title").asText());
						milestone.setDescription(milestoneNode.get("description").asText(null));
						milestone.setProject(project);
						String dueDateString = milestoneNode.get("due_on").asText(null);
						if (dueDateString != null) 
							milestone.setDueDate(ISODateTimeFormat.dateTimeNoMillis().parseDateTime(dueDateString).toDate());
						if (milestoneNode.get("state").asText().equals("closed"))
							milestone.setClosed(true);
						
						milestones.put(milestoneNode.get("number").asLong(), milestone);
						project.getMilestones().add(milestone);
						
						if (!dryRun)
							OneDev.getInstance(MilestoneManager.class).save(milestone);
					}
					
					logger.log("Importing issues from repository " + projectMapping.getGitHubRepo() + "...");
					GitHubImportResult currentResult = importIssues(importSource, projectMapping.getGitHubRepo(), 
							project, true, importOption, users, dryRun, logger);
					result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
					result.nonExistentMilestones.addAll(currentResult.nonExistentMilestones);
					result.unmappedIssueLabels.addAll(currentResult.unmappedIssueLabels);

					result.issuesImported |= currentResult.issuesImported;
				}
			}
			
			return result.toHtml("Repositories imported successfully");
		} catch (Exception e) {
			for (Long projectId: projectIds)
				OneDev.getInstance(StorageManager.class).deleteProjectDir(projectId);
			throw new RuntimeException(e);
		} finally {
			client.close();
		}
	}
		
}