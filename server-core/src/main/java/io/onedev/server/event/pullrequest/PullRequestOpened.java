package io.onedev.server.event.pullrequest;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class PullRequestOpened extends PullRequestEvent implements MarkdownAware, CommitAware {

	public PullRequestOpened(PullRequest request) {
		super(request.getSubmitter(), request.getSubmitDate(), request);
	}

	@Override
	public String getMarkdown() {
		return getRequest().getDescription();
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "opened";
		if (withEntity)
			activity += " pull request " + getRequest().getNumberAndTitle();
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
