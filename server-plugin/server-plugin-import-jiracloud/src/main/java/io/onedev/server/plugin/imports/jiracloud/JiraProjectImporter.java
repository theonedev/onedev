package io.onedev.server.plugin.imports.jiracloud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.JerseyUtils;

public class JiraProjectImporter extends ProjectImporter<ImportServer, ProjectImportSource, ImportOption> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return ImportUtils.NAME;
	}

	@Override
	public ProjectImportSource getWhat(ImportServer where, TaskLogger logger) {
		ProjectImportSource importSource = new ProjectImportSource();
		for (String projectName: ImportUtils.getProjectNodes(where, logger).keySet()) {
			ProjectMapping mapping = new ProjectMapping();
			mapping.setJiraProject(projectName);
			mapping.setOneDevProject(projectName.replace(' ', '-'));
			importSource.getProjectMappings().add(mapping);
		}
		return importSource;
	}

	@Override
	public ImportOption getHow(ImportServer where, ProjectImportSource what, TaskLogger logger) {
		List<String> jiraProjects = what.getProjectMappings().stream()
				.map(it->it.getJiraProject()).collect(Collectors.toList());
		return ImportUtils.buildImportOption(where, jiraProjects, logger);
	}

	@Override
	public String doImport(ImportServer where, ProjectImportSource what, ImportOption how, 
		boolean dryRun, TaskLogger logger) {
		Collection<Long> projectIds = new ArrayList<>();
		
		Map<String, JsonNode> projectNodes = ImportUtils.getProjectNodes(where, logger);
		
		Client client = where.newClient();
		try {
			Map<String, Optional<User>> users = new HashMap<>();
			ImportResult result = new ImportResult();
			
			for (ProjectMapping projectMapping: what.getProjectMappings()) {
				String jiraProject = projectMapping.getJiraProject();
				JsonNode projectNode = projectNodes.get(jiraProject);
				if (projectNode == null)
					throw new ExplicitException("Unable to find project: " + jiraProject);

				String apiEndpoint = where.getApiEndpoint("/project/" + projectNode.get("id").asText());
				
				// Get more detail project information
				projectNode = JerseyUtils.get(client, apiEndpoint, logger);
				
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);				
				Project project = projectManager.initialize(projectMapping.getOneDevProject());
				Preconditions.checkState(project.isNew());
				
				project.setDescription(projectNode.get("description").asText(null));
				
				if (!dryRun) {
					OneDev.getInstance(ProjectManager.class).create(project);
					projectIds.add(project.getId());
				}
				
				logger.log("Importing issues from project " + jiraProject + "...");
				ImportResult currentResult = ImportUtils.importIssues(where, projectNode, 
						project, true, how, users, dryRun, logger);
				result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
				result.errorAttachments.addAll(currentResult.errorAttachments);
				result.tooLargeAttachments.addAll(currentResult.tooLargeAttachments);
				result.unmappedIssuePriorities.addAll(currentResult.unmappedIssuePriorities);
				result.unmappedIssueStatuses.addAll(currentResult.unmappedIssueStatuses);
				result.unmappedIssueTypes.addAll(currentResult.unmappedIssueTypes);
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
