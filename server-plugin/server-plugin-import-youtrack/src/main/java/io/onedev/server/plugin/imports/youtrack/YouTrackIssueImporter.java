package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.model.Project;
import io.onedev.server.web.util.ImportStep;

public class YouTrackIssueImporter implements IssueImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Authenticate to YouTrack";
		}

		@Override
		protected ImportServer newSetting() {
			return new ImportServer();
		}
		
	};
	
	private final ImportStep<ImportProject> projectStep = new ImportStep<ImportProject>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Choose project";
		}

		@Override
		protected ImportProject newSetting() {
			ImportProject project = new ImportProject();
			project.server = serverStep.getSetting();
			return project;
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
			ImportServer server = serverStep.getSetting();
			String project = projectStep.getSetting().getProject();
			return server.buildImportOption(project);
		}
		
	};
	
	@Override
	public String getName() {
		return YouTrackPluginModule.NAME;
	}

	@Override
	public String doImport(Project project, boolean retainIssueNumbers, boolean dryRun, TaskLogger logger) {
		ImportServer server = serverStep.getSetting();
		String youTrackProject = projectStep.getSetting().getProject();
		ImportOption option = optionStep.getSetting();
		
		return server.importIssues(project, youTrackProject, option, retainIssueNumbers, dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, projectStep, optionStep);
	}

}