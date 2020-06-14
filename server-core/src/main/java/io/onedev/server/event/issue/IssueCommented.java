package io.onedev.server.event.issue;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.IssueComment;

public class IssueCommented extends IssueEvent implements MarkdownAware {

	private final IssueComment comment;
	
	public IssueCommented(IssueComment comment) {
		super(comment.getUser(), comment.getDate(), comment.getIssue());
		this.comment = comment;
	}

	public IssueComment getComment() {
		return comment;
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public boolean affectsBoards() {
		return false;
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "commented";
		if (withEntity)
			activity += " on issue " + comment.getIssue().getNumberAndTitle();
		return activity;
	}

}
