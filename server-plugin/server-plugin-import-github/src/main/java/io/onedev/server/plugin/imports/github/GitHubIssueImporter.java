package io.onedev.server.plugin.imports.github;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.web.util.ImportStep;

public class GitHubIssueImporter implements IssueImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Authenticate to GitHub";
		}

		@Override
		protected ImportServer newSetting() {
			return new ImportServer();
		}
		
	};
	
	private final ImportStep<ImportRepository> repositoryStep = new ImportStep<ImportRepository>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Choose repository";
		}

		@Override
		protected ImportRepository newSetting() {
			ImportRepository repository = new ImportRepository();
			repository.server = serverStep.getSetting();
			return repository;
		}
		
	};
	
	private final ImportStep<IssueImportOption> optionStep = new ImportStep<IssueImportOption>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Specify import option";
		}

		@Override
		protected IssueImportOption newSetting() {
			return serverStep.getSetting().buildIssueImportOption(
					Lists.newArrayList(repositoryStep.getSetting().getRepository()));
		}
		
	};
	
	@Override
	public String getName() {
		return GitHubPluginModule.NAME;
	}
	
	@Override
	public String doImport(Project project, boolean retainIssueNumbers, boolean dryRun, TaskLogger logger) {
		logger.log("Importing issues from repository " + repositoryStep.getSetting().getRepository() + "...");
		Map<String, Optional<User>> users = new HashMap<>();
		
		ImportResult result = serverStep.getSetting().importIssues(repositoryStep.getSetting().getRepository(), 
				project,retainIssueNumbers, optionStep.getSetting(), users, dryRun, logger);
		return result.toHtml("Issues imported successfully");
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, repositoryStep, optionStep);
	}

}