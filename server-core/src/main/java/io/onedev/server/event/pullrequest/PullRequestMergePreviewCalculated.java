package io.onedev.server.event.pullrequest;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class PullRequestMergePreviewCalculated extends PullRequestEvent implements CommitAware {
	
	public PullRequestMergePreviewCalculated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public String getActivity() {
		return "Merge preview calculated";
	}

	@Override
	public ProjectScopedCommit getCommit() {
		MergePreview mergePreview = getRequest().getMergePreview();
		if (mergePreview != null && mergePreview.getMergeCommitHash() != null)
			return new ProjectScopedCommit(getProject(), ObjectId.fromString(mergePreview.getMergeCommitHash()));
		else
			return new ProjectScopedCommit(getProject(), ObjectId.zeroId());
	}

	@Override
	public PullRequestEvent cloneIn(Dao dao) {
		return new PullRequestMergePreviewCalculated(dao.load(PullRequest.class, getRequest().getId()));
	}

}
