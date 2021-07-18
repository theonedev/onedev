package io.onedev.server.plugin.imports.gitlab;

import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.NAME;
import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.buildImportOption;
import static io.onedev.server.plugin.imports.gitlab.GitLabImportUtils.importIssues;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.page.project.issues.imports.IssueImporter;

public class GitLabIssueImporter extends IssueImporter<GitLabIssueImportSource, GitLabIssueImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public GitLabIssueImportOption getImportOption(GitLabIssueImportSource importSource, SimpleLogger logger) {
		return buildImportOption(importSource, importSource.getProject(), logger);
	}

	@Override
	public String doImport(Project project, GitLabIssueImportSource importSource, GitLabIssueImportOption importOption,
			boolean dryRun, SimpleLogger logger) {
		logger.log("Importing issues from project " + importSource.getProject() + "...");
		Map<String, Optional<User>> users = new HashMap<>();
		return importIssues(importSource, importSource.getProject(), project, false, importOption, users, dryRun, logger)
				.toHtml("Issues imported successfully");
	}
	
}