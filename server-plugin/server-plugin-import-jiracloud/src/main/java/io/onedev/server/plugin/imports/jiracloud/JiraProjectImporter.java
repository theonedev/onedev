package io.onedev.server.plugin.imports.jiracloud;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.util.ImportStep;

public class JiraProjectImporter extends ProjectImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Authenticate to JIRA cloud");
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
			return _T("Specify projects");
		}

		@Override
		protected ImportProjects newSetting() {
			ImportProjects projects;
			if (getParentProjectPath() != null)
				projects = new ChildrenImportProjects();
			else
				projects = new ImportProjects();
			projects.server = serverStep.getSetting();
			return projects;
		}
		
	};
	
	private final ImportStep<ImportOption> optionStep = new ImportStep<ImportOption>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Specify import option");
		}

		@Override
		protected ImportOption newSetting() {
			return serverStep.getSetting().buildImportOption(projectsStep.getSetting().getImportProjects());
		}
		
	};
	
	@Override
	public String getName() {
		return JiraModule.NAME;
	}

	@Override
	public TaskResult doImport(boolean dryRun, TaskLogger logger) {
		return OneDev.getInstance(TransactionService.class).call(() -> {
			ImportProjects projects = projectsStep.getSetting();
			if (getParentProjectPath() != null) 
				projects.setParentOneDevProject(getParentProjectPath());
			return serverStep.getSetting().importProjects(projects, optionStep.getSetting(), dryRun, logger);
		});
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, projectsStep, optionStep);
	}

}
