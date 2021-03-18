package io.onedev.server.event.pullrequest;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class PullRequestChangeEvent extends PullRequestEvent implements MarkdownAware, CommitAware {

	private final PullRequestChange change;
	
	public PullRequestChangeEvent(PullRequestChange change) {
		super(change.getUser(), change.getDate(), change.getRequest());
		this.change = change;
	}

	public PullRequestChange getChange() {
		return change;
	}

	@Override
	public String getMarkdown() {
		if (change.getData().getCommentAware() != null)
			return change.getData().getCommentAware().getComment();
		else
			return null;
	}

	@Override
	public String getActivity(boolean withEntity) {
		return change.getData().getActivity(withEntity?change.getRequest():null);
	}

	@Override
	public ProjectScopedCommit getCommit() {
		ObjectId commitId;
		if (change.getData() instanceof PullRequestMergeData) {
			MergePreview preview = getRequest().getMergePreview();
			commitId = ObjectId.fromString(preview.getMergeCommitHash());
		} else if (change.getData() instanceof PullRequestDiscardData) {
			commitId = ObjectId.fromString(getRequest().getLatestUpdate().getTargetHeadCommitHash());
		} else {
			commitId = ObjectId.zeroId();
		}
		return new ProjectScopedCommit(getProject(), commitId);
	}

}
