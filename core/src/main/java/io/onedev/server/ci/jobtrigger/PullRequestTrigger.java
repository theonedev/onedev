package io.onedev.server.ci.jobtrigger;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.event.pullrequest.PullRequestUpdated;
import io.onedev.server.web.editable.annotation.BranchPattern;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.utils.PathUtils;

@Editable(order=300, name="When pull requests are created/updated")
public class PullRequestTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String targetBranches;
	
	@Editable(order=100)
	@BranchPattern
	public String getTargetBranches() {
		return targetBranches;
	}

	public void setTargetBranches(String targetBranches) {
		this.targetBranches = targetBranches;
	}

	@Override
	protected boolean matches(ProjectEvent event, CISpec ciSpec, Job job) {
		if (event instanceof PullRequestOpened) {
			PullRequestOpened pullRequestOpened = (PullRequestOpened) event;
			if (PathUtils.matchChildAware(getTargetBranches(), pullRequestOpened.getRequest().getTargetBranch()))
				return true;
		} 
		if (event instanceof PullRequestUpdated) {
			PullRequestUpdated pullRequestUpdated = (PullRequestUpdated) event;
			if (PathUtils.matchChildAware(getTargetBranches(), pullRequestUpdated.getRequest().getTargetBranch()))
				return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		if (getTargetBranches() != null)
			return "When pull requests targeting branch '" + getTargetBranches() + "'are created/updated";
		else
			return "When pull requests are created/updated";
	}

}
