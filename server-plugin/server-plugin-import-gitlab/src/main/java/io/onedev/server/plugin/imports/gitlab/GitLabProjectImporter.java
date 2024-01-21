package io.onedev.server.plugin.imports.gitlab;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.util.ImportStep;

import java.io.Serializable;
import java.util.List;

public class GitLabProjectImporter implements ProjectImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Authenticate to GitLab";
		}

		@Override
		protected ImportServer newSetting() {
			return new ImportServer();
		}

	};
	
	private final ImportStep<ImportProjects> projectsStep = new ImportStep<ImportProjects>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Specify projects";
		}

		@Override
		protected ImportProjects newSetting() {
			ImportProjects projects = new ImportProjects();
			projects.server = serverStep.getSetting();
			return projects;
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
			option.setIssueImportOption(serverStep.getSetting().buildIssueImportOption(projectsStep.getSetting().getImportProjects()));
			return option;
		}
		
	};
	
	@Override
	public String getName() {
		return GitLabModule.NAME;
	}
	
	@Override
	public TaskResult doImport(boolean dryRun, TaskLogger logger) {
		ImportServer server = serverStep.getSetting();
		return server.importProjects(projectsStep.getSetting(), optionStep.getSetting(), dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, projectsStep, optionStep);
	}
		
}