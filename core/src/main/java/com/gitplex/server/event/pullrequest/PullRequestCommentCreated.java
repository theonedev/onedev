package com.gitplex.server.event.pullrequest;

import java.util.Date;

import com.gitplex.server.event.MarkdownAware;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequestComment;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="commented")
public class PullRequestCommentCreated extends PullRequestEvent implements MarkdownAware {

	private final PullRequestComment comment;
	
	public PullRequestCommentCreated(PullRequestComment comment) {
		super(comment.getRequest());
		this.comment = comment;
	}

	public PullRequestComment getComment() {
		return comment;
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public Account getUser() {
		return comment.getUser();
	}

	@Override
	public Date getDate() {
		return comment.getDate();
	}

}
