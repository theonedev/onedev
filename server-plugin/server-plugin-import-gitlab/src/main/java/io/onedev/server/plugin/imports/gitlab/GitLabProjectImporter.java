package io.onedev.server.plugin.imports.gitlab;

import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.NAME;
import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.buildImportOption;
import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.get;
import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.importIssues;
import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.list;
import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.newClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Client;

import org.apache.http.client.utils.URIBuilder;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.page.project.imports.ProjectImporter;

public class GitLabProjectImporter extends ProjectImporter<GitLabProjectImportSource, GitLabProjectImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public GitLabProjectImportOption getImportOption(GitLabProjectImportSource importSource, SimpleLogger logger) {
		GitLabProjectImportOption importOption = new GitLabProjectImportOption();
		Client client = newClient(importSource);
		try {
			String apiEndpoint = importSource.getApiEndpoint("/projects?membership=true");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) {
				String pathWithNamespace = projectNode.get("path_with_namespace").asText();
				ProjectMapping projectMapping = new ProjectMapping();
				projectMapping.setGitLabProject(pathWithNamespace);
				projectMapping.setOneDevProject(pathWithNamespace.replace('/', '-'));
				importOption.getProjectMappings().add(projectMapping);
			}					
			GitLabIssueImportOption issueImportOption = buildImportOption(importSource, null, logger);
			importOption.setAssigneesIssueField(issueImportOption.getAssigneesIssueField());
			importOption.setClosedIssueState(issueImportOption.getClosedIssueState());
			importOption.setIssueLabelMappings(issueImportOption.getIssueLabelMappings());
		} finally {
			client.close();
		}
		return importOption;
	}
	
	private List<Milestone> getMilestones(GitLabProjectImportSource importSource, 
			String groupId, SimpleLogger logger) {
		Client client = newClient(importSource);
		try {
			List<Milestone> milestones = new ArrayList<>();
			String apiEndpoint = importSource.getApiEndpoint("/groups/" + groupId + "/milestones");
			for (JsonNode milestoneNode: list(client, apiEndpoint, logger)) 
				milestones.add(getMilestone(milestoneNode));
			apiEndpoint = importSource.getApiEndpoint("/groups/" + groupId);
			JsonNode groupNode = get(client, apiEndpoint, logger);
			JsonNode parentIdNode = groupNode.get("parent_id");
			if (parentIdNode != null && parentIdNode.asText(null) != null) 
				milestones.addAll(getMilestones(importSource, parentIdNode.asText(), logger));
			return milestones;
		} finally {
			client.close();
		}
	}
	
	private Milestone getMilestone(JsonNode milestoneNode) {
		Milestone milestone = new Milestone();
		milestone.setName(milestoneNode.get("title").asText());
		milestone.setDescription(milestoneNode.get("description").asText(null));
		String dueDateString = milestoneNode.get("due_date").asText(null);
		if (dueDateString != null) 
			milestone.setDueDate(ISODateTimeFormat.date().parseDateTime(dueDateString).toDate());
		if (milestoneNode.get("state").asText().equals("closed"))
			milestone.setClosed(true);
		return milestone;
	}
	
	@Override
	public String doImport(GitLabProjectImportSource importSource, GitLabProjectImportOption importOption, 
			boolean dryRun, SimpleLogger logger) {
		Collection<Long> projectIds = new ArrayList<>();
		Client client = newClient(importSource);
		try {
			Map<String, Optional<User>> users = new HashMap<>();
			GitLabImportResult result = new GitLabImportResult();
			for (ProjectMapping projectMapping: importOption.getProjectMappings()) {
				logger.log("Cloning code from project " + projectMapping.getGitLabProject() + "...");
				
				String apiEndpoint = importSource.getApiEndpoint("/projects/" + projectMapping.getGitLabProject().replace("/", "%2F"));
				JsonNode projectNode = get(client, apiEndpoint, logger);
				Project project = new Project();
				project.setName(projectMapping.getOneDevProject());
				project.setDescription(projectNode.get("description").asText(null));
				project.setIssueManagementEnabled(projectNode.get("issues_enabled").asBoolean());
				
				String visibility = projectNode.get("visibility").asText();
				if (!visibility.equals("private") && importOption.getPublicRole() != null)
					project.setDefaultRole(importOption.getPublicRole());
				
				URIBuilder builder = new URIBuilder(projectNode.get("http_url_to_repo").asText());
				if (!visibility.equals("public"))
					builder.setUserInfo("git", importSource.getAccessToken());
				
				if (!dryRun) {
					OneDev.getInstance(ProjectManager.class).clone(project, builder.build().toString());
					projectIds.add(project.getId());
				}

				if (projectMapping.isImportIssues()) {
					List<Milestone> milestones = new ArrayList<>();
					logger.log("Importing milestones from project " + projectMapping.getGitLabProject() + "...");
					apiEndpoint = importSource.getApiEndpoint("/projects/" 
							+ projectMapping.getGitLabProject().replace("/", "%2F") + "/milestones");
					for (JsonNode milestoneNode: list(client, apiEndpoint, logger)) 
						milestones.add(getMilestone(milestoneNode));
					JsonNode namespaceNode = projectNode.get("namespace");
					if (namespaceNode.get("kind").asText().equals("group")) {
						String groupId = namespaceNode.get("id").asText();
						milestones.addAll(getMilestones(importSource, groupId, logger));
					}

					for (Milestone milestone: milestones) {
						milestone.setProject(project);
						project.getMilestones().add(milestone);
						if (!dryRun)
							OneDev.getInstance(MilestoneManager.class).save(milestone);
					}
					
					logger.log("Importing issues from project " + projectMapping.getGitLabProject() + "...");
					GitLabImportResult currentResult = importIssues(importSource, projectMapping.getGitLabProject(), 
							project, true, importOption, users, dryRun, logger);
					result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
					result.nonExistentMilestones.addAll(currentResult.nonExistentMilestones);
					result.unmappedIssueLabels.addAll(currentResult.unmappedIssueLabels);
					result.tooLargeAttachments.addAll(currentResult.tooLargeAttachments);
					result.errorAttachments.addAll(currentResult.errorAttachments);
				}
			}
			
			return result.toHtml("Projects imported successfully");
		} catch (Exception e) {
			for (Long projectId: projectIds)
				OneDev.getInstance(StorageManager.class).deleteProjectDir(projectId);
			throw new RuntimeException(e);
		} finally {
			client.close();
		}
	}
		
}