package io.onedev.server.ci.jobtrigger;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.ci.Job;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.web.editable.annotation.BranchPatterns;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300, name="When pull requests are created/updated")
public class PullRequestTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String targetBranches;
	
	@Editable(order=100, description="Optionally specify target branches of the pull request to check. "
			+ "Use * or ? for wildcard match. Leave empty to match all pull requests")
	@BranchPatterns
	public String getTargetBranches() {
		return targetBranches;
	}

	public void setTargetBranches(String targetBranches) {
		this.targetBranches = targetBranches;
	}
	
	@Override
	public boolean matches(ProjectEvent event, Job job) {
		if (event instanceof PullRequestMergePreviewCalculated) {
			PullRequestMergePreviewCalculated pullRequestMergePreviewCalculated = (PullRequestMergePreviewCalculated) event;
			if (getTargetBranches() == null 
					|| PathUtils.matchChildAware(getTargetBranches(), pullRequestMergePreviewCalculated.getRequest().getTargetBranch())) { 
				return true;
			}
		} 
		return false;
	}

	@Override
	public String getDescription() {
		if (getTargetBranches() != null)
			return String.format("When pull requests to branches '%s' are created/updated", getTargetBranches());
		else
			return "When pull requests are created/updated";
	}

}
