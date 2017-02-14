package com.gitplex.server.core.event.pullrequest;

import com.gitplex.server.core.entity.PullRequest;

public class PullRequestPendingApproval extends PullRequestEvent {

	public PullRequestPendingApproval(PullRequest request) {
		super(request);
	}

}
