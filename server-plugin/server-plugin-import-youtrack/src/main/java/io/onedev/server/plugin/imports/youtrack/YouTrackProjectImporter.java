package io.onedev.server.plugin.imports.youtrack;

import static io.onedev.server.plugin.imports.youtrack.ImportUtils.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.model.Project;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.web.util.WicketUtils;

public class YouTrackProjectImporter extends ProjectImporter<ImportServer, ProjectImportSource, ImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return ImportUtils.NAME;
	}

	@Override
	public String doImport(ImportServer where, ProjectImportSource what, ImportOption how, 
			boolean dryRun, TaskLogger logger) {
		Map<String, String> youTrackProjectIds = new HashMap<>();
		Map<String, String> youTrackProjectDescriptions = new HashMap<>();
		Collection<Long> projectIds = new ArrayList<>();
		
		Client client = where.newClient();
		try {
			String apiEndpoint = where.getApiEndpoint("/admin/projects?fields=id,name,description");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) { 
				String projectName = projectNode.get("name").asText();
				youTrackProjectIds.put(projectName, projectNode.get("id").asText());
				youTrackProjectDescriptions.put(projectName, projectNode.get("description").asText(null));
			}
			
			ImportResult result = new ImportResult();
			
			for (ProjectMapping projectMapping: what.getProjectMappings()) {
				logger.log("Importing project '" + projectMapping.getYouTrackProject() + "'...");
				
				String youTrackProjectId = youTrackProjectIds.get(projectMapping.getYouTrackProject());
				if (youTrackProjectId == null)
					throw new ExplicitException("Unable to find YouTrack project: " + projectMapping.getYouTrackProject());
				
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);				
				Project project = projectManager.initialize(projectMapping.getOneDevProject());
				Preconditions.checkState(project.isNew());
				project.setDescription(youTrackProjectDescriptions.get(projectMapping.getYouTrackProject()));
				project.setIssueManagement(true);
				
		       	if (!dryRun) {
					projectManager.create(project);
					projectIds.add(project.getId());
		       	}

		       	ImportResult currentResult = ImportUtils.importIssues(where, youTrackProjectId, project, 
		       			true, how, dryRun, logger);
		       	result.mismatchedIssueFields.putAll(currentResult.mismatchedIssueFields);
		       	result.nonExistentLogins.addAll(currentResult.nonExistentLogins);
		       	result.tooLargeAttachments.addAll(currentResult.tooLargeAttachments);
		       	result.unmappedIssueFields.addAll(currentResult.unmappedIssueFields);
		       	result.unmappedIssueStates.addAll(currentResult.unmappedIssueStates);
		       	result.unmappedIssueLinks.addAll(currentResult.unmappedIssueLinks);
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

	@Override
	public ProjectImportSource getWhat(ImportServer where, TaskLogger logger) {
		WicketUtils.getPage().setMetaData(ImportServer.META_DATA_KEY, where);
		return new ProjectImportSource();
	}

	@Override
	public ImportOption getHow(ImportServer where, ProjectImportSource what, TaskLogger logger) {
		Client client = where.newClient();
		try {
			List<JsonNode> projectNodes = new ArrayList<>();
			String apiEndpoint = where.getApiEndpoint("/admin/projects?fields=id,name,customFields(field(name),bundle(values(name)))");
			Set<String> projectNames = what.getProjectMappings().stream()
					.map(it->it.getYouTrackProject()).collect(Collectors.toSet()); 
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) {
				if (projectNames.contains(projectNode.get("name").asText())) 
					projectNodes.add(projectNode);
			}
			
			return ImportUtils.buildImportOption(where, projectNodes, logger);
		} finally {
			client.close();
		}
	}
	
}