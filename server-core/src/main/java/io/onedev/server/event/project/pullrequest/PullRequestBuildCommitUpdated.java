package io.onedev.server.event.project.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;
import org.eclipse.jgit.lib.ObjectId;

import java.util.Date;

public class PullRequestBuildCommitUpdated extends PullRequestEvent implements CommitAware {
	
	private static final long serialVersionUID = 1L;

	public PullRequestBuildCommitUpdated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public String getActivity() {
		return "Build commit updated";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public ProjectScopedCommit getCommit() {
		if (getRequest().getBuildCommitHash() != null)
			return new ProjectScopedCommit(getProject(), ObjectId.fromString(getRequest().getBuildCommitHash()));
		else
			return new ProjectScopedCommit(getProject(), ObjectId.zeroId());
	}

}
