package io.onedev.server.plugin.imports.jiracloud;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.web.util.ImportStep;

public class JiraProjectImporter implements ProjectImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Authenticate to JIRA cloud";
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
				mapping.setJiraProject(project);
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
			List<String> jiraProjects = projectsStep.getSetting().getProjectMappings().stream()
					.map(it->it.getJiraProject()).collect(Collectors.toList());
			return serverStep.getSetting().buildImportOption(jiraProjects);
		}
		
	};
	
	@Override
	public String getName() {
		return JiraModule.NAME;
	}

	@Override
	public String doImport(boolean dryRun, TaskLogger logger) {
		return serverStep.getSetting().importProjects(
				projectsStep.getSetting(), optionStep.getSetting(), dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, projectsStep, optionStep);
	}

}
