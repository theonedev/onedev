package io.onedev.server.event.issue;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.Editable;

@Editable(name="commented")
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
		return comment.getUser();
	}

	@Override
	public Date getDate() {
		return comment.getDate();
	}

}
