package io.onedev.server.plugin.imports.github;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.util.ImportStep;

public class GitHubProjectImporter extends ProjectImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Authenticate to GitHub");
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
			return _T("Specify repositories");
		}

		@Override
		protected ImportRepositories newSetting() {
			ImportRepositories repositories;
			if (getParentProjectPath() != null)
				repositories = new ChildrenImportRepositories();
			else
				repositories = new ImportRepositories();
			repositories.server = serverStep.getSetting();
			return repositories;
		}
		
	};
	
	private final ImportStep<ProjectImportOption> optionStep = new ImportStep<ProjectImportOption>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Specify import option");
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
		return GitHubModule.NAME;
	}
	
	@Override
	public TaskResult doImport(boolean dryRun, TaskLogger logger) {
		ImportServer server = serverStep.getSetting();
		ImportRepositories repositories = repositoriesStep.getSetting();
		if (getParentProjectPath() != null) 
			repositories.setParentOneDevProject(getParentProjectPath());
		ProjectImportOption option = optionStep.getSetting();
		return server.importProjects(repositories, option, dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, repositoriesStep, optionStep);
	}
		
}