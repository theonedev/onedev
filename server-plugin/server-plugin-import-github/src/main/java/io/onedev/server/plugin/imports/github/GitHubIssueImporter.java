package io.onedev.server.plugin.imports.github;

import static io.onedev.server.plugin.imports.github.GitHubImportUtils.NAME;
import static io.onedev.server.plugin.imports.github.GitHubImportUtils.buildImportOption;
import static io.onedev.server.plugin.imports.github.GitHubImportUtils.importIssues;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.page.project.issues.imports.IssueImporter;

public class GitHubIssueImporter extends IssueImporter<GitHubIssueImportSource, GitHubIssueImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public GitHubIssueImportOption getImportOption(GitHubIssueImportSource importSource, SimpleLogger logger) {
		GitHubIssueImportOption importOption;
		if (importSource.isPrepopulateImportOptions()) 
			importOption = buildImportOption(importSource, importSource.getRepository(), logger);
		else 
			importOption = new GitHubIssueImportOption();
		return importOption;
	}

	@Override
	public String doImport(Project project, GitHubIssueImportSource importSource, GitHubIssueImportOption importOption,
			boolean dryRun, SimpleLogger logger) {
		logger.log("Importing issues from repository " + importSource.getRepository() + "...");
		Map<String, Optional<User>> users = new HashMap<>();
		return importIssues(importSource, importSource.getRepository(), project, false, importOption, users, dryRun, logger)
				.toHtml("Issues imported successfully");
	}
	
}