package io.onedev.server.event.project.pullrequest;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class PullRequestChanged extends PullRequestEvent implements CommitAware {

	private static final long serialVersionUID = 1L;

	private final Long changeId;
	
	private final String note;
	
	public PullRequestChanged(PullRequestChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), change.getRequest());
		changeId = change.getId();
		this.note = note;
	}

	public PullRequestChange getChange() {
		return OneDev.getInstance(PullRequestChangeManager.class).load(changeId);
	}

	@Override
	protected CommentText newCommentText() {
		return note!=null? new MarkdownText(getProject(), note): null;
	}
	
	@Nullable
	public String getComment() {
		return note;
	}

	@Override
	public String getActivity() {
		return getChange().getData().getActivity();
	}

	@Override
	public ProjectScopedCommit getCommit() {
		ObjectId commitId;
		if (getChange().getData() instanceof PullRequestMergeData) {
			MergePreview preview = getRequest().getMergePreview();
			if (preview != null)
				commitId = ObjectId.fromString(preview.getMergeCommitHash());
			else
				commitId = ObjectId.zeroId(); // Merged outside
		} else if (getChange().getData() instanceof PullRequestDiscardData) {
			commitId = ObjectId.fromString(getRequest().getLatestUpdate().getTargetHeadCommitHash());
		} else {
			commitId = ObjectId.zeroId();
		}
		return new ProjectScopedCommit(getProject(), commitId);
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getChange());
	}

}
