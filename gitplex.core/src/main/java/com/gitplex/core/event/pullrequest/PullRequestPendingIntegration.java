package com.gitplex.core.event.pullrequest;

import com.gitplex.core.entity.PullRequest;

public class PullRequestPendingIntegration extends PullRequestEvent {

	public PullRequestPendingIntegration(PullRequest request) {
		super(request);
	}

}
