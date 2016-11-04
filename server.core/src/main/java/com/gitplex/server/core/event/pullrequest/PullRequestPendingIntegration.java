package com.gitplex.server.core.event.pullrequest;

import com.gitplex.server.core.entity.PullRequest;

public class PullRequestPendingIntegration extends PullRequestEvent {

	public PullRequestPendingIntegration(PullRequest request) {
		super(request);
	}

}
