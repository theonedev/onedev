package io.onedev.server.plugin.imports.youtrack;

import static io.onedev.server.plugin.imports.youtrack.YouTrackImportUtils.NAME;
import static io.onedev.server.plugin.imports.youtrack.YouTrackImportUtils.buildImportOption;
import static io.onedev.server.plugin.imports.youtrack.YouTrackImportUtils.importIssues;
import static io.onedev.server.plugin.imports.youtrack.YouTrackImportUtils.list;
import static io.onedev.server.plugin.imports.youtrack.YouTrackImportUtils.newClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.page.project.imports.ProjectImporter;

public class YouTrackProjectImporter extends ProjectImporter<YouTrackProjectImportSource, YouTrackProjectImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public YouTrackProjectImportOption getImportOption(YouTrackProjectImportSource importSource, SimpleLogger logger) {
		YouTrackProjectImportOption importOption = new YouTrackProjectImportOption();
		if (importSource.isPrepopulateImportOptions()) {
			Client client = newClient(importSource);
			try {
				String apiEndpoint = importSource.getApiEndpoint("/admin/projects?fields=name");
				for (JsonNode projectNode: list(client, apiEndpoint, logger)) {
					ProjectMapping projectMapping = new ProjectMapping();
					projectMapping.setYouTrackProject(projectNode.get("name").asText());
					projectMapping.setOneDevProject(projectMapping.getYouTrackProject().replace(' ', '-'));
					importOption.getProjectMappings().add(projectMapping);
				}
				YouTrackIssueImportOption issueImportOption = buildImportOption(importSource, null, logger);
				importOption.setIssueFieldMappings(issueImportOption.getIssueFieldMappings());
				importOption.setIssueStateMappings(issueImportOption.getIssueStateMappings());
				importOption.setIssueTagMappings(issueImportOption.getIssueTagMappings());
			} finally {
				client.close();
			}
		}
		return importOption;
	}
	
	@Override
	public String doImport(YouTrackProjectImportSource importSource, YouTrackProjectImportOption importOption, 
			boolean dryRun, SimpleLogger logger) {
		
		Map<String, String> youTrackProjectIds = new HashMap<>();
		Map<String, String> youTrackProjectDescriptions = new HashMap<>();
		Collection<Long> projectIds = new ArrayList<>();
		
		Client client = newClient(importSource);
		try {
			String apiEndpoint = importSource.getApiEndpoint("/admin/projects?fields=id,name,description");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) { 
				String projectName = projectNode.get("name").asText();
				youTrackProjectIds.put(projectName, projectNode.get("id").asText());
				youTrackProjectDescriptions.put(projectName, projectNode.get("description").asText(null));
			}
			
			YouTrackImportResult result = new YouTrackImportResult();
			
			for (ProjectMapping projectMapping: importOption.getProjectMappings()) {
				logger.log("Importing project '" + projectMapping.getYouTrackProject() + "'...");
				
				String youTrackProjectId = youTrackProjectIds.get(projectMapping.getYouTrackProject());
				if (youTrackProjectId == null)
					throw new ExplicitException("Unable to find YouTrack project: " + projectMapping.getYouTrackProject());
				
				Project project = new Project();
				project.setName(projectMapping.getOneDevProject());
				project.setDescription(youTrackProjectDescriptions.get(projectMapping.getYouTrackProject()));
				project.setIssueManagementEnabled(true);
				
		       	if (!dryRun) {
					OneDev.getInstance(ProjectManager.class).create(project);
					projectIds.add(project.getId());
		       	}

		       	YouTrackImportResult currentResult = importIssues(importSource, youTrackProjectId, project, 
		       			true, importOption, dryRun, logger);
		       	result.mismatchedIssueFields.putAll(currentResult.mismatchedIssueFields);
		       	result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
		       	result.tooLargeAttachments.addAll(currentResult.tooLargeAttachments);
		       	result.unmappedIssueFields.addAll(currentResult.unmappedIssueFields);
		       	result.unmappedIssueStates.addAll(currentResult.unmappedIssueStates);
		       	result.unmappedIssueTags.addAll(currentResult.unmappedIssueTags);
			}		       	

			return result.toHtml("Projects imported successfully");
		} catch (Exception e) {
			for (Long projectId: projectIds)
				OneDev.getInstance(StorageManager.class).deleteProjectDir(projectId);
			throw ExceptionUtils.unchecked(e);
		} finally {
			client.close();
		}			
	}
	
}