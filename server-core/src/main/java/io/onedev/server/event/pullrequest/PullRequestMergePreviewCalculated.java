package io.onedev.server.event.pullrequest;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class PullRequestMergePreviewCalculated extends PullRequestEvent implements CommitAware {
	
	public PullRequestMergePreviewCalculated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "Merge preview calculated";
		if (withEntity)
			activity += " in pull request " + getRequest().getNumberAndTitle();
		return activity;
	}

	@Override
	public ProjectScopedCommit getCommit() {
		MergePreview mergePreview = getRequest().getMergePreview();
		if (mergePreview != null && mergePreview.getMergeCommitHash() != null)
			return new ProjectScopedCommit(getProject(), ObjectId.fromString(mergePreview.getMergeCommitHash()));
		else
			return new ProjectScopedCommit(getProject(), ObjectId.zeroId());
	}

}
