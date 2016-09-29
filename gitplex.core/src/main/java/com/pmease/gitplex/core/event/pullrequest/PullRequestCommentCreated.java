package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.event.MarkdownAware;

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
