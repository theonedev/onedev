package io.onedev.server.plugin.imports.youtrack;

import static io.onedev.server.plugin.imports.youtrack.YouTrackImportUtils.*;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.page.project.issues.imports.IssueImporter;

public class YouTrackIssueImporter extends IssueImporter<YouTrackIssueImportSource, YouTrackIssueImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}

	private String getYouTrackProjectId(YouTrackProjectImportSource importSource, 
			String youTrackProjectName, SimpleLogger logger) {
		Client client = newClient(importSource);
		try {
			String apiEndpoint = importSource.getApiEndpoint("/admin/projects?fields=id,name");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) {
				if (youTrackProjectName.equals(projectNode.get("name").asText())) 
					return projectNode.get("id").asText();
			}
			throw new ExplicitException("Unable to find YouTrack project: " + youTrackProjectName);
		} finally {
			client.close();
		}
	}
	
	@Override
	public YouTrackIssueImportOption getImportOption(YouTrackIssueImportSource importSource, SimpleLogger logger) {
		if (importSource.isPrepopulateImportOptions()) {
			String youTrackProjectId = getYouTrackProjectId(importSource, importSource.getProject(), logger);
			return buildImportOption(importSource, youTrackProjectId, logger);
		} else {
			return new YouTrackIssueImportOption();
		}
	}
	
	@Override
	public String doImport(Project project, YouTrackIssueImportSource importSource, 
			YouTrackIssueImportOption importOption, boolean dryRun, SimpleLogger logger) {
		logger.log("Importing issues from '" + importSource.getProject() + "'...");
		String youTrackProjectId = getYouTrackProjectId(importSource, importSource.getProject(), logger);
		return importIssues(importSource, youTrackProjectId, project, false, importOption, dryRun, logger)
				.toHtml("Issues imported successfully");
	}
	
}