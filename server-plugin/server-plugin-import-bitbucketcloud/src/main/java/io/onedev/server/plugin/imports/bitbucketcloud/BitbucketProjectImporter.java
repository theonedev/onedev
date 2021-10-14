package io.onedev.server.plugin.imports.bitbucketcloud;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.client.Client;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.model.Project;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.JerseyUtils;

public class BitbucketProjectImporter extends ProjectImporter<ImportServer, ProjectImportSource, ProjectImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return ImportUtils.NAME;
	}
	
	@Override
	public String doImport(ImportServer where, ProjectImportSource what, ProjectImportOption how, 
			boolean dryRun, TaskLogger logger) {
		Collection<Long> projectIds = new ArrayList<>();
		Client client = where.newClient();
		try {
			for (ProjectMapping projectMapping: what.getProjectMappings()) {
				logger.log("Cloning code from repository " + projectMapping.getBitbucketRepo() + "...");
				
				String apiEndpoint = where.getApiEndpoint("/repositories/" + projectMapping.getBitbucketRepo());
				JsonNode repoNode = JerseyUtils.get(client, apiEndpoint, logger);
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);                               
				Project project = projectManager.initialize(projectMapping.getOneDevProject());
				Preconditions.checkState(project.isNew());				
				
				project.setDescription(repoNode.get("description").asText(null));
				
				boolean isPrivate = repoNode.get("is_private").asBoolean();
				if (!isPrivate && how.getPublicRole() != null)
					project.setDefaultRole(how.getPublicRole());

				String cloneUrl = null;
				for (JsonNode cloneNode: repoNode.get("links").get("clone")) {
					if (cloneNode.get("name").asText().equals("https")) {
						cloneUrl = cloneNode.get("href").asText();
						break;
					}
				}
				if (cloneUrl == null)
					throw new ExplicitException("Https clone url not found");
				
				URIBuilder builder = new URIBuilder(cloneUrl);
				if (isPrivate)
					builder.setUserInfo(where.getUserName(), where.getAppPassword());
				
				if (!dryRun) {
					projectManager.clone(project, builder.build().toString());
					projectIds.add(project.getId());
				}
			}
			
			return "Repositories imported successfully";
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
			String apiEndpoint = where.getApiEndpoint("/repositories?role=member");
			for (JsonNode repoNode: ImportUtils.list(client, apiEndpoint, logger)) {
				String fullName = repoNode.get("full_name").asText();
				ProjectMapping projectMapping = new ProjectMapping();
				projectMapping.setBitbucketRepo(fullName);
				projectMapping.setOneDevProject(fullName);
				importSource.getProjectMappings().add(projectMapping);
			}					
		} finally {
			client.close();
		}
		return importSource;
	}

	@Override
	public ProjectImportOption getHow(ImportServer where, ProjectImportSource what, TaskLogger logger) {
		return new ProjectImportOption();
	}
		
}