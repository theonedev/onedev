package io.onedev.server.plugin.imports.gitlab;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.web.util.ImportStep;

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
	
	private final ImportStep<ImportGroup> groupStep = new ImportStep<ImportGroup>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Choose group";
		}

		@Override
		protected ImportGroup newSetting() {
			ImportGroup group = new ImportGroup();
			group.server = serverStep.getSetting();
			return group;
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
			String groupId = groupStep.getSetting().getGroupId();
			for (String project: serverStep.getSetting().listProjects(groupId)) {
				ProjectMapping projectMapping = new ProjectMapping();
				projectMapping.setGitLabProject(project);
				projectMapping.setOneDevProject(project);
				projects.getProjectMappings().add(projectMapping);
			}
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
			List<String> gitLabProjects = projectsStep.getSetting().getProjectMappings().stream()
					.map(it->it.getGitLabProject()).collect(Collectors.toList());
			option.setIssueImportOption(serverStep.getSetting().buildIssueImportOption(gitLabProjects));
			return option;
		}
		
	};
	
	@Override
	public String getName() {
		return GitLabPluginModule.NAME;
	}
	
	@Override
	public String doImport(boolean dryRun, TaskLogger logger) {
		ImportServer server = serverStep.getSetting();
		return server.importProjects(projectsStep.getSetting(), optionStep.getSetting(), dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, groupStep, projectsStep, optionStep);
	}
		
}