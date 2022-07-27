package io.onedev.server.plugin.imports.gitea;

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

public class GiteaIssueImporter implements IssueImporter {

	private static final long serialVersionUID = 1L;

	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Authenticate to Gitea";
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
		return GiteaPluginModule.NAME;
	}

	@Override
	public String doImport(Project project, boolean retainIssueNumbers, boolean dryRun, TaskLogger logger) {
		ImportServer server = serverStep.getSetting();
		String giteaRepo = repositoryStep.getSetting().getRepository();
		IssueImportOption option = optionStep.getSetting();
		
		logger.log("Importing issues from repository " + giteaRepo + "...");
		Map<String, Optional<User>> users = new HashMap<>();
		
		return server.importIssues(giteaRepo, project, retainIssueNumbers, option, users, dryRun, logger)
				.toHtml("Issues imported successfully");
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, repositoryStep, optionStep);
	}

}
