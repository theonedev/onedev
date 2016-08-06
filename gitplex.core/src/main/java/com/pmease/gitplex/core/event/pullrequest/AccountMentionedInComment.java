package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequestComment;

public class AccountMentionedInComment extends PullRequestNotificationEvent {

	private final PullRequestComment comment;
	
	private final Account user;
	
	public AccountMentionedInComment(PullRequestComment comment, Account user) {
		super(comment.getRequest());
		this.comment = comment;
		this.user = user;
	}

	public PullRequestComment getComment() {
		return comment;
	}

	public Account getUser() {
		return user;
	}

}
