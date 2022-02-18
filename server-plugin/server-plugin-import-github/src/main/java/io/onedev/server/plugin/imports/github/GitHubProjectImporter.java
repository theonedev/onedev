package io.onedev.server.plugin.imports.github;

import static io.onedev.server.plugin.imports.github.ImportUtils.NAME;
import static io.onedev.server.plugin.imports.github.ImportUtils.get;
import static io.onedev.server.plugin.imports.github.ImportUtils.importIssues;
import static io.onedev.server.plugin.imports.github.ImportUtils.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;

import org.apache.http.client.utils.URIBuilder;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.storage.StorageManager;

public class GitHubProjectImporter extends ProjectImporter<ImportServer, ProjectImportSource, ProjectImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String doImport(ImportServer where, ProjectImportSource what, ProjectImportOption how, 
			boolean dryRun, TaskLogger logger) {
		Collection<Long> projectIds = new ArrayList<>();
		Client client = where.newClient();
		try {
			Map<String, Optional<User>> users = new HashMap<>();
			ImportResult result = new ImportResult();
			for (ProjectMapping projectMapping: what.getProjectMappings()) {
				logger.log("Cloning code from repository " + projectMapping.getGitHubRepo() + "...");
				
				String apiEndpoint = where.getApiEndpoint("/repos/" + projectMapping.getGitHubRepo());
				JsonNode repoNode = get(client, apiEndpoint, logger);
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);				
				Project project = projectManager.initialize(projectMapping.getOneDevProject());
				Preconditions.checkState(project.isNew());
				project.setDescription(repoNode.get("description").asText(null));
				project.setIssueManagement(repoNode.get("has_issues").asBoolean());
				
				boolean isPrivate = repoNode.get("private").asBoolean();
				if (!isPrivate && how.getPublicRole() != null)
					project.setDefaultRole(how.getPublicRole());
				
				URIBuilder builder = new URIBuilder(repoNode.get("clone_url").asText());
				if (isPrivate)
					builder.setUserInfo("git", where.getAccessToken());
				
				if (!dryRun) {
					projectManager.clone(project, builder.build().toString());
					projectIds.add(project.getId());
				}

				if (how.getIssueImportOption() != null) {
					List<Milestone> milestones = new ArrayList<>();
					logger.log("Importing milestones from repository " + projectMapping.getGitHubRepo() + "...");
					apiEndpoint = where.getApiEndpoint("/repos/" + projectMapping.getGitHubRepo() + "/milestones?state=all");
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
						
						milestones.add(milestone);
						project.getMilestones().add(milestone);
						
						if (!dryRun)
							OneDev.getInstance(MilestoneManager.class).save(milestone);
					}
					
					logger.log("Importing issues from repository " + projectMapping.getGitHubRepo() + "...");
					ImportResult currentResult = importIssues(where, projectMapping.getGitHubRepo(), 
							project, true, how.getIssueImportOption(), users, dryRun, logger);
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

	@Override
	public ProjectImportSource getWhat(ImportServer where, TaskLogger logger) {
		ProjectImportSource importSource = new ProjectImportSource();
		Client client = where.newClient();
		try {
			String apiEndpoint = where.getApiEndpoint("/user/repos");
			for (JsonNode repoNode: list(client, apiEndpoint, logger)) {
				String repoName = repoNode.get("name").asText();
				String ownerName = repoNode.get("owner").get("login").asText();
				ProjectMapping projectMapping = new ProjectMapping();
				projectMapping.setGitHubRepo(ownerName + "/" + repoName);
				projectMapping.setOneDevProject(ownerName + "/" + repoName);
				importSource.getProjectMappings().add(projectMapping);
			}					
		} finally {
			client.close();
		}
		return importSource;
	}

	@Override
	public ProjectImportOption getHow(ImportServer where, ProjectImportSource what, TaskLogger logger) {
		ProjectImportOption importOption = new ProjectImportOption();
		List<String> gitHubRepos = what.getProjectMappings().stream()
				.map(it->it.getGitHubRepo()).collect(Collectors.toList());
		importOption.setIssueImportOption(ImportUtils.buildImportOption(where, gitHubRepos, logger));
		return importOption;
	}
		
}