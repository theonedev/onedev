package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.BranchName;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.pullrequesttitleanddescriptionprovider.GeneratedPullRequestTitleAndDescription;
import io.onedev.server.buildspec.step.pullrequesttitleanddescriptionprovider.PullRequestTitleAndDescriptionProvider;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(name="Create Pull Request", order=350)
public class CreatePullRequestStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
		
	private String targetBranch;
	
	private String sourceBranch;

	private PullRequestTitleAndDescriptionProvider titleAndDescritpionProvider = new GeneratedPullRequestTitleAndDescription();

	private MergeStrategy mergeStrategy;

	private boolean workInProgress = true;
	
	@Editable(order=1000, description="Specify name of the target branch")
	@Interpolative(variableSuggester="suggestVariables")
	@BranchName
	@NotEmpty
	public String getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

	@Editable(order=1050, description="Specify name of the source branch")
	@Interpolative(variableSuggester="suggestVariables")
	@BranchName
	@NotEmpty
	public String getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

	@Editable(order=1100, name="Title and Description")
	@NotNull
	public PullRequestTitleAndDescriptionProvider getTitleAndDescritpionProvider() {
		return titleAndDescritpionProvider;
	}

	public void setTitleAndDescritpionProvider(PullRequestTitleAndDescriptionProvider titleProvider) {
		this.titleAndDescritpionProvider = titleProvider;
	}

	@Editable(order=1200, description="Specify merge strategy of the pull request. Leave empty to use default merge strategy of the project")
	public MergeStrategy getMergeStrategy() {
		return mergeStrategy;
	}

	public void setMergeStrategy(MergeStrategy mergeStrategy) {
		this.mergeStrategy = mergeStrategy;
	}

	@Editable(order=1250, description="Specify whether to create a work in progress pull request")
	public boolean isWorkInProgress() {
		return workInProgress;
	}

	public void setWorkInProgress(boolean workInProgress) {
		this.workInProgress = workInProgress;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestRevisions(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestRevisions(project, matchWith);
		else
			return new ArrayList<>();
	}

	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return OneDev.getInstance(SessionService.class).call(() -> {
			if (StringUtils.isBlank(getTargetBranch())) 
				throw new ExplicitException("Target branch should not be empty");
			if (StringUtils.isBlank(getSourceBranch())) 
				throw new ExplicitException("Source branch should not be empty");
			
			var build = OneDev.getInstance(BuildService.class).load(buildId);
			
			var projectId = build.getProject().getId();

			ProjectAndBranch target = new ProjectAndBranch(projectId, getTargetBranch());
			ProjectAndBranch source = new ProjectAndBranch(projectId, getSourceBranch());

			if (target.equals(source)) {
				logger.warning("Pull request will not be created as target branch is the same as source branch");
			} else {
				var pullRequestService = OneDev.getInstance(PullRequestService.class);
				var pullRequest = pullRequestService.findOpen(target, source);
				if (pullRequest != null) {
					logger.warning("A pull request has already been opened for specified branches");
				} else {
					pullRequest = new PullRequest();
					pullRequest.setTarget(target);
					pullRequest.setSource(source);
					pullRequest.setMergeStrategy(getMergeStrategy());
					pullRequest.setSubmitter(OneDev.getInstance(UserService.class).getSystem());
	
					var baseCommitId = OneDev.getInstance(GitService.class).getMergeBase(
						target.getProject(), target.getObjectId(), 
						source.getProject(), source.getObjectId());
	
					if (baseCommitId == null)
						throw new ExplicitException("No common base for target and source branches");
	
					if (baseCommitId.name().equals(source.getObjectName())) {
						logger.warning("Pull request will not be created as target branch is already up to date with source branch");
					} else {
						pullRequest.setBaseCommitHash(baseCommitId.name());
	
						var update = new PullRequestUpdate();
						pullRequest.getUpdates().add(update);
						pullRequest.setUpdates(pullRequest.getUpdates());
						update.setRequest(pullRequest);
						update.setHeadCommitHash(source.getObjectName());
						update.setTargetHeadCommitHash(pullRequest.getTarget().getObjectName());						
							
						var titleAndDescription = titleAndDescritpionProvider.getTitleAndDescription(pullRequest);
						
						pullRequest.setTitle(titleAndDescription.getLeft());
						if (isWorkInProgress() && !pullRequest.isWorkInProgress()) 
							pullRequest.setTitle("[WIP] " + pullRequest.getTitle());
						
						pullRequest.setDescription(titleAndDescription.getRight());
						
						pullRequestService.open(pullRequest);	
					}	
				}	
			}
			return new ServerStepResult(true);
		});
	}

}
