package com.gitplex.core.event.pullrequest;

import java.util.Date;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.PullRequestComment;
import com.gitplex.core.event.MarkdownAware;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="commented")
public class PullRequestCommentCreated extends PullRequestChangeEvent implements MarkdownAware {

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
