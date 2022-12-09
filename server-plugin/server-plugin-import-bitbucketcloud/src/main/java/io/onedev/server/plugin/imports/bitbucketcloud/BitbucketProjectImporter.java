package io.onedev.server.plugin.imports.bitbucketcloud;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.web.util.ImportStep;

public class BitbucketProjectImporter implements ProjectImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Authenticate to Bitbucket Cloud";
		}

		@Override
		protected ImportServer newSetting() {
			return new ImportServer();
		}
		
	};
	
	private final ImportStep<ImportWorkspace> workspaceStep = new ImportStep<ImportWorkspace>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Choose workspace";
		}

		@Override
		protected ImportWorkspace newSetting() {
			ImportWorkspace workspace = new ImportWorkspace();
			workspace.server = serverStep.getSetting();
			return workspace;
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
			String workspace = workspaceStep.getSetting().getWorkspace();
			for (String repository: serverStep.getSetting().listRepositories(
					workspace, workspaceStep.getSetting().isIncludeForks())) {
				ProjectMapping projectMapping = new ProjectMapping();
				projectMapping.setBitbucketRepo(repository);
				projectMapping.setOneDevProject(repository);
				repositories.getProjectMappings().add(projectMapping);
			}
			
			return repositories;
		}
		
	};
	
	private final ImportStep<ImportOption> optionStep = new ImportStep<ImportOption>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Specify import option";
		}

		@Override
		protected ImportOption newSetting() {
			return new ImportOption();
		}
		
	};
	
	@Override
	public String getName() {
		return BitbucketModule.NAME;
	}
	
	@Override
	public String doImport(boolean dryRun, TaskLogger logger) {
		ImportRepositories repositories = repositoriesStep.getSetting();
		ImportOption option = optionStep.getSetting();
		return serverStep.getSetting().importProjects(repositories, option, dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, workspaceStep, repositoriesStep, optionStep);
	}

}