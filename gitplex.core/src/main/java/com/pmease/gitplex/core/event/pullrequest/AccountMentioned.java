package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

public class AccountMentioned extends PullRequestNotificationEvent {

	private final Account user;
	
	public AccountMentioned(PullRequest request, Account user) {
		super(request);
		this.user = user;
	}

	public Account getUser() {
		return user;
	}

}
