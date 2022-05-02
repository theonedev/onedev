package io.onedev.server.plugin.imports.youtrack;

import static io.onedev.server.plugin.imports.youtrack.ImportUtils.NAME;
import static io.onedev.server.plugin.imports.youtrack.ImportUtils.importIssues;
import static io.onedev.server.plugin.imports.youtrack.ImportUtils.list;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.JsonNode;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.model.Project;
import io.onedev.server.web.util.WicketUtils;

public class YouTrackIssueImporter extends IssueImporter<ImportServer, IssueImportSource, ImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String doImport(ImportServer where, IssueImportSource what, ImportOption how, Project project,
			boolean retainIssueNumbers, boolean dryRun, TaskLogger logger) {
		logger.log("Importing issues from '" + what.getProject() + "'...");
		Client client = where.newClient();
		try {
			String apiEndpoint = where.getApiEndpoint("/admin/projects?fields=id,name");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) {
				if (what.getProject().equals(projectNode.get("name").asText())) { 
					ImportResult result = importIssues(where, projectNode.get("id").asText(), 
							project, retainIssueNumbers, how, dryRun, logger);
					return result.toHtml("Issues imported successfully");
				}
			}
			throw new ExplicitException("Unable to find YouTrack project: " + what.getProject());
		} finally {
			client.close();
		}
	}

	@Override
	public IssueImportSource getWhat(ImportServer where, TaskLogger logger) {
		WicketUtils.getPage().setMetaData(ImportServer.META_DATA_KEY, where);
		return new IssueImportSource();
	}

	@Override
	public ImportOption getHow(ImportServer where, IssueImportSource what, TaskLogger logger) {
		Client client = where.newClient();
		try {
			String apiEndpoint = where.getApiEndpoint("/admin/projects?fields=id,name,customFields(field(name),bundle(values(name)))");
			for (JsonNode projectNode: list(client, apiEndpoint, logger)) {
				if (what.getProject().equals(projectNode.get("name").asText())) {  
					List<JsonNode> projectNodes = new ArrayList<>();
					projectNodes.add(projectNode);
					return ImportUtils.buildImportOption(where, projectNodes, logger);
				}
			}
			throw new ExplicitException("Unable to find YouTrack project: " + what.getProject());
		} finally {
			client.close();
		}
	}
	
}