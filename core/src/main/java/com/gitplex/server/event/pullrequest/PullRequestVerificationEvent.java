package com.gitplex.server.event.pullrequest;

import java.util.Date;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.User;

public abstract class PullRequestVerificationEvent extends PullRequestEvent {

	private Date date;
	
	public PullRequestVerificationEvent(PullRequest request) {
		super(request);
		date = new Date();
	}

	@Override
	public User getUser() {
		return null;
	}

	@Override
	public Date getDate() {
		return date;
	}

}
