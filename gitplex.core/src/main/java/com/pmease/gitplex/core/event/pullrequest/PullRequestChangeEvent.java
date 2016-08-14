package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

public abstract class PullRequestChangeEvent extends PullRequestEvent {

	private final Account user;
	
	private final Date date;
	
	public PullRequestChangeEvent(PullRequest request, @Nullable Account user, Date date) {
		super(request);
		this.user = user;
		this.date = date;
	}

	@Nullable
	public Account getUser() {
		return user;
	}

	public Date getDate() {
		return date;
	}

}
