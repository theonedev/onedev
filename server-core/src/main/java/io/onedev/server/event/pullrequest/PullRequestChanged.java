package io.onedev.server.event.pullrequest;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class PullRequestChanged extends PullRequestEvent implements CommitAware {

	private final PullRequestChange change;
	
	private final String note;
	
	public PullRequestChanged(PullRequestChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), change.getRequest());
		this.change = change;
		this.note = note;
	}

	public PullRequestChange getChange() {
		return change;
	}

	@Override
	public String getMarkdown() {
		return note;
	}
	
	@Nullable
	public String getComment() {
		return note;
	}

	@Override
	public String getActivity() {
		return change.getData().getActivity();
	}

	@Override
	public ProjectScopedCommit getCommit() {
		ObjectId commitId;
		if (change.getData() instanceof PullRequestMergeData) {
			MergePreview preview = getRequest().getMergePreview();
			if (preview != null)
				commitId = ObjectId.fromString(preview.getMergeCommitHash());
			else
				commitId = ObjectId.zeroId(); // Merged outside
		} else if (change.getData() instanceof PullRequestDiscardData) {
			commitId = ObjectId.fromString(getRequest().getLatestUpdate().getTargetHeadCommitHash());
		} else {
			commitId = ObjectId.zeroId();
		}
		return new ProjectScopedCommit(getProject(), commitId);
	}

}
