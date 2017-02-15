package com.gitplex.server.event.pullrequest;

import com.gitplex.server.entity.PullRequest;

public class PullRequestPendingIntegration extends PullRequestEvent {

	public PullRequestPendingIntegration(PullRequest request) {
		super(request);
	}

}
