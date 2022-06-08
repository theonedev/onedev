package io.onedev.server.event.pullrequest;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.PullRequest;

public class PullRequestCodeCommentStatusChanged extends PullRequestCodeCommentEvent {

	private final CodeCommentStatusChange change;
	
	private final String note;
	
	public PullRequestCodeCommentStatusChanged(PullRequest request, CodeCommentStatusChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), request, change.getComment());
		this.change = change;
		this.note = note;
	}

	public CodeCommentStatusChange getChange() {
		return change;
	}

	@Override
	public String getMarkdown() {
		return note;
	}

	@Override
	public String getActivity() {
		if (change.isResolved())
			return "resolved code comment"; 
		else
			return "unresolved code comment";
	}

}
