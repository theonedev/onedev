package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.PullRequest;

public abstract class PullRequestEvent {

	private final PullRequest request;
	
	public PullRequestEvent(PullRequest request) {
		this.request = request;
	}

	public PullRequest getRequest() {
		return request;
	}
	
}
