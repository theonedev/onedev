package io.onedev.server.event.issue;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;

public class IssueCommented extends IssueEvent implements MarkdownAware {

	private final IssueComment comment;
	
	public IssueCommented(IssueComment comment) {
		super(comment.getIssue());
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
	public User getUser() {
		return User.getForDisplay(comment.getUser(), comment.getUserName());
	}

	@Override
	public Date getDate() {
		return comment.getDate();
	}

}
