package io.onedev.server.plugin.imports.gitlab;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.taskbutton.TaskResult.HtmlMessgae;
import io.onedev.server.web.util.ImportStep;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GitLabIssueImporter implements IssueImporter {

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
	
	private final ImportStep<IssueImportOption> optionStep = new ImportStep<IssueImportOption>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return "Specify import option";
		}

		@Override
		protected IssueImportOption newSetting() {
			return serverStep.getSetting().buildIssueImportOption(
					Lists.newArrayList(projectStep.getSetting().getProject()));
		}
		
	};
	
	@Override
	public String getName() {
		return GitLabModule.NAME;
	}
	
	@Override
	public TaskResult doImport(Long projectId, boolean dryRun, TaskLogger logger) {
		return OneDev.getInstance(TransactionManager.class).call(() -> {
			var project = OneDev.getInstance(ProjectManager.class).load(projectId);
			ImportServer server = serverStep.getSetting();
			String gitLabProject = projectStep.getSetting().getProject();
			IssueImportOption option = optionStep.getSetting();
			logger.log("Importing issues from project " + gitLabProject + "...");
			Map<String, Optional<Long>> userIds = new HashMap<>();

			ImportResult result = server.importIssues(gitLabProject, project, option, userIds, dryRun, logger);
			return new TaskResult(true, new HtmlMessgae(result.toHtml("Issues imported successfully")));
		});
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, projectStep, optionStep);
	}

}