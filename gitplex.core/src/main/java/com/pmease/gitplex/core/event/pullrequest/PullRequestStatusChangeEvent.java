package com.pmease.gitplex.core.event.pullrequest;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

public abstract class PullRequestStatusChangeEvent extends PullRequestChangeEvent {

	private final Account user;
	
	private final String note;
	
	public PullRequestStatusChangeEvent(PullRequest request, Account user, @Nullable String note) {
		super(request);
		this.user = user;
		this.note = note;
	}

	public Account getUser() {
		return user;
	}

	@Nullable
	public String getNote() {
		return note;
	}

}
