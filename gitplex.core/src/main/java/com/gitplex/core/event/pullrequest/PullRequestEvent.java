package com.gitplex.core.event.pullrequest;

import com.gitplex.core.entity.PullRequest;

public abstract class PullRequestEvent {

	private final PullRequest request;
	
	public PullRequestEvent(PullRequest request) {
		this.request = request;
	}

	public PullRequest getRequest() {
		return request;
	}
	
}
