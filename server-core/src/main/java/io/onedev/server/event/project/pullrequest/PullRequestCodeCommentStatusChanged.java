package io.onedev.server.event.project.pullrequest;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentStatusChangeManager;
import io.onedev.server.web.UrlManager;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class PullRequestCodeCommentStatusChanged extends PullRequestCodeCommentEvent {

	private static final long serialVersionUID = 1L;

	private final Long changeId;
	
	private final String note;
	
	public PullRequestCodeCommentStatusChanged(PullRequest request, 
			CodeCommentStatusChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), request, change.getComment());
		changeId = change.getId();
		this.note = note;
	}

	public CodeCommentStatusChange getChange() {
		return OneDev.getInstance(CodeCommentStatusChangeManager.class).load(changeId);
	}

	@Override
	protected CommentText newCommentText() {
		return note!=null? new MarkdownText(getProject(), note): null;
	}

	@Override
	public String getActivity() {
		if (getChange().isResolved())
			return "resolved code comment"; 
		else
			return "unresolved code comment";
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getChange());
	}

}
