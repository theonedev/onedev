package io.onedev.server.plugin.imports.gitlab;

import static io.onedev.server.plugin.imports.gitlab.ImportUtils.NAME;
import static io.onedev.server.plugin.imports.gitlab.ImportUtils.list;

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
import io.onedev.server.util.JerseyUtils;

public class GitLabProjectImporter extends ProjectImporter<ImportServer, ProjectImportSource, ProjectImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	private List<Milestone> getMilestones(ImportServer server, String groupId, TaskLogger logger) {
		Client client = server.newClient();
		try {
			List<Milestone> milestones = new ArrayList<>();
			String apiEndpoint = server.getApiEndpoint("/groups/" + groupId + "/milestones");
			for (JsonNode milestoneNode: list(client, apiEndpoint, logger)) 
				milestones.add(getMilestone(milestoneNode));
			apiEndpoint = server.getApiEndpoint("/groups/" + groupId);
			JsonNode groupNode = JerseyUtils.get(client, apiEndpoint, logger);
			JsonNode parentIdNode = groupNode.get("parent_id");
			if (parentIdNode != null && parentIdNode.asText(null) != null) 
				milestones.addAll(getMilestones(server, parentIdNode.asText(), logger));
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
	public String doImport(ImportServer where, ProjectImportSource what, ProjectImportOption how, 
			boolean dryRun, TaskLogger logger) {
		Collection<Long> projectIds = new ArrayList<>();
		Client client = where.newClient();
		try {
			Map<String, Optional<User>> users = new HashMap<>();
			ImportResult result = new ImportResult();
			for (ProjectMapping projectMapping: what.getProjectMappings()) {
				logger.log("Cloning code from project " + projectMapping.getGitLabProject() + "...");
				
				String apiEndpoint = where.getApiEndpoint("/projects/" + projectMapping.getGitLabProject().replace("/", "%2F"));
				JsonNode projectNode = JerseyUtils.get(client, apiEndpoint, logger);
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);				
				Project project = projectManager.initialize(projectMapping.getOneDevProject());
				Preconditions.checkState(project.isNew());
				project.setDescription(projectNode.get("description").asText(null));
				project.setIssueManagement(projectNode.get("issues_enabled").asBoolean());
				
				String visibility = projectNode.get("visibility").asText();
				if (!visibility.equals("private") && how.getPublicRole() != null)
					project.setDefaultRole(how.getPublicRole());
				
				URIBuilder builder = new URIBuilder(projectNode.get("http_url_to_repo").asText());
				if (!visibility.equals("public"))
					builder.setUserInfo("git", where.getAccessToken());
				
				if (!dryRun) {
					projectManager.clone(project, builder.build().toString());
					projectIds.add(project.getId());
				}

				if (how.getIssueImportOption() != null) {
					List<Milestone> milestones = new ArrayList<>();
					logger.log("Importing milestones from project " + projectMapping.getGitLabProject() + "...");
					apiEndpoint = where.getApiEndpoint("/projects/" 
							+ projectMapping.getGitLabProject().replace("/", "%2F") + "/milestones");
					for (JsonNode milestoneNode: list(client, apiEndpoint, logger)) 
						milestones.add(getMilestone(milestoneNode));
					JsonNode namespaceNode = projectNode.get("namespace");
					if (namespaceNode.get("kind").asText().equals("group")) {
						String groupId = namespaceNode.get("id").asText();
						milestones.addAll(getMilestones(where, groupId, logger));
					}

					for (Milestone milestone: milestones) {
						milestone.setProject(project);
						project.getMilestones().add(milestone);
						if (!dryRun)
							OneDev.getInstance(MilestoneManager.class).save(milestone);
					}
					
					logger.log("Importing issues from project " + projectMapping.getGitLabProject() + "...");
					ImportResult currentResult = ImportUtils.importIssues(where, projectMapping.getGitLabProject(), 
							project, true, how.getIssueImportOption(), users, dryRun, logger);
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

	@Override
	public ProjectImportSource getWhat(ImportServer where, TaskLogger logger) {
		ProjectImportSource importSource = new ProjectImportSource();
		Client client = where.newClient();
		try {
			String apiEndpoint = where.getApiEndpoint("/projects?membership=true");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) {
				String pathWithNamespace = projectNode.get("path_with_namespace").asText();
				ProjectMapping projectMapping = new ProjectMapping();
				projectMapping.setGitLabProject(pathWithNamespace);
				projectMapping.setOneDevProject(pathWithNamespace);
				importSource.getProjectMappings().add(projectMapping);
			}					
		} finally {
			client.close();
		}
		return importSource;
	}

	@Override
	public ProjectImportOption getHow(ImportServer where, ProjectImportSource what, TaskLogger logger) {
		List<String> gitLabProjects = what.getProjectMappings().stream()
				.map(it->it.getGitLabProject()).collect(Collectors.toList());
		ProjectImportOption importOption = new ProjectImportOption();
		importOption.setIssueImportOption(ImportUtils.buildImportOption(where, gitLabProjects, logger));
		return importOption;
	}
		
}