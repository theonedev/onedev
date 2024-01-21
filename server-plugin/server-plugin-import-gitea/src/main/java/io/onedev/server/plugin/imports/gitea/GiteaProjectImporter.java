package io.onedev.server.plugin.imports.gitea;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.util.ImportStep;

import java.io.Serializable;
import java.util.List;

public class GiteaProjectImporter implements ProjectImporter {

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
	
	private final ImportStep<ImportRepositories> repositoriesStep = new ImportStep<ImportRepositories>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Specify repositories";
		}

		@Override
		protected ImportRepositories newSetting() {
			ImportRepositories repositories = new ImportRepositories();
			repositories.server = serverStep.getSetting();
			return repositories;
		}
		
	};
	
	private final ImportStep<ProjectImportOption> optionStep = new ImportStep<ProjectImportOption>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Specify import option";
		}

		@Override
		protected ProjectImportOption newSetting() {
			ProjectImportOption option = new ProjectImportOption();
			option.setIssueImportOption(serverStep.getSetting().buildIssueImportOption(repositoriesStep.getSetting().getImportRepositories()));
			return option;
		}
		
	};
	
	@Override
	public String getName() {
		return GiteaModule.NAME;
	}

	@Override
	public TaskResult doImport(boolean dryRun, TaskLogger logger) {
		ImportRepositories repositories = repositoriesStep.getSetting();
		ProjectImportOption option = optionStep.getSetting();
		return serverStep.getSetting().importProjects(repositories, option, dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, repositoriesStep, optionStep);
	}

}
