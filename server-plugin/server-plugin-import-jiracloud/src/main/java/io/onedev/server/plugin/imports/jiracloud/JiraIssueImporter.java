package io.onedev.server.plugin.imports.jiracloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.web.util.WicketUtils;

public class JiraIssueImporter extends IssueImporter<ImportServer, IssueImportSource, ImportOption> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return ImportUtils.NAME;
	}

	@Override
	public IssueImportSource getWhat(ImportServer where, TaskLogger logger) {
		WicketUtils.getPage().setMetaData(ImportServer.META_DATA_KEY, where);
		return new IssueImportSource();
	}

	@Override
	public ImportOption getHow(ImportServer where, IssueImportSource what, TaskLogger logger) {
		return ImportUtils.buildImportOption(where, Lists.newArrayList(what.getProject()), logger);
	}

	@Override
	public String doImport(ImportServer where, IssueImportSource what, ImportOption how, 
			Project project, boolean retainIssueNumbers, boolean dryRun, TaskLogger logger) {
		Map<String, JsonNode> projectNodes = ImportUtils.getProjectNodes(where, logger);
		
		Client client = where.newClient();
		try {
			Map<String, Optional<User>> users = new HashMap<>();
			JsonNode projectNode = projectNodes.get(what.getProject());
			if (projectNode == null)
				throw new ExplicitException("Unable to find project: " + what.getProject());
			ImportResult result = ImportUtils.importIssues(where, projectNode, project, retainIssueNumbers, 
					how, users, dryRun, logger);
			return result.toHtml("Issues imported successfully");
		} finally {
			client.close();
		}
	}

}
