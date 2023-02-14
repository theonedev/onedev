package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class PullRequestMergePreviewUpdated extends PullRequestEvent implements CommitAware {
	
	private static final long serialVersionUID = 1L;

	public PullRequestMergePreviewUpdated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public String getActivity() {
		return "Merge preview updated";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public ProjectScopedCommit getCommit() {
		MergePreview mergePreview = getRequest().checkMergePreview();
		if (mergePreview != null && mergePreview.getMergeCommitHash() != null)
			return new ProjectScopedCommit(getProject(), ObjectId.fromString(mergePreview.getMergeCommitHash()));
		else
			return new ProjectScopedCommit(getProject(), ObjectId.zeroId());
	}

}
