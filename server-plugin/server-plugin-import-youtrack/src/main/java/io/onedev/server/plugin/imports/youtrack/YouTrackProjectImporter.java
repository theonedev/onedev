package io.onedev.server.plugin.imports.youtrack;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.web.util.ImportStep;

import java.io.Serializable;
import java.util.List;

public class YouTrackProjectImporter implements ProjectImporter {

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
	
	private final ImportStep<ImportProjects> projectsStep = new ImportStep<ImportProjects>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Specify projects";
		}

		@Override
		protected ImportProjects newSetting() {
			ImportProjects projects = new ImportProjects();
			for (String project: serverStep.getSetting().listProjects()) {
				ProjectMapping mapping = new ProjectMapping();
				mapping.setYouTrackProject(project);
				mapping.setOneDevProject(project.replace(' ', '-'));
				projects.getProjectMappings().add(mapping);
			}
			return projects;
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
			ImportProjects projects = projectsStep.getSetting();
			return server.buildImportOption(projects);
		}
		
	};
	
	@Override
	public String getName() {
		return YouTrackModule.NAME;
	}

	@Override
	public String doImport(boolean dryRun, TaskLogger logger) {
		ImportServer server = serverStep.getSetting();
		ImportProjects projects = projectsStep.getSetting();
		ImportOption option = optionStep.getSetting();
		return server.importProjects(projects, option, dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, projectsStep, optionStep);
	}

}