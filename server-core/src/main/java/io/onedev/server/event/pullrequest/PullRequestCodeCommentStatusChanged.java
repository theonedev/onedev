package io.onedev.server.event.pullrequest;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.PullRequest;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class PullRequestCodeCommentStatusChanged extends PullRequestCodeCommentEvent {

	private final CodeCommentStatusChange change;
	
	private final String note;
	
	public PullRequestCodeCommentStatusChanged(PullRequest request, 
			CodeCommentStatusChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), request, change.getComment());
		this.change = change;
		this.note = note;
	}

	public CodeCommentStatusChange getChange() {
		return change;
	}

	@Override
	protected CommentText newCommentText() {
		return note!=null? new MarkdownText(getProject(), note): null;
	}

	@Override
	public String getActivity() {
		if (change.isResolved())
			return "resolved code comment"; 
		else
			return "unresolved code comment";
	}

	@Override
	public PullRequestEvent cloneIn(Dao dao) {
		return new PullRequestCodeCommentStatusChanged(
				dao.load(PullRequest.class, getRequest().getId()), 
				dao.load(CodeCommentStatusChange.class, change.getId()), 
				note);
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getChange());
	}

}
