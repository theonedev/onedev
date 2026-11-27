package io.onedev.server.event.project.pullrequest;

import java.io.Serial;
import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScopedCommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class PullRequestBuildCommitUpdated extends PullRequestEvent implements ProjectScopedCommitAware {
	
	@Serial
	private static final long serialVersionUID = 1L;

	private final ObjectId commitId;

	public PullRequestBuildCommitUpdated(PullRequest request) {
		super(null, new Date(), request);
		String hash = request.getBuildCommitHash();
		commitId = hash != null ? ObjectId.fromString(hash) : ObjectId.zeroId();
	}

	@Override
	public String getActivity() {
		return "build commit updated";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public ProjectScopedCommit getProjectScopedCommit() {
		return new ProjectScopedCommit(getProject(), commitId);
	}

}
