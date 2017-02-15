package com.gitplex.server.event.pullrequest;

import com.gitplex.server.entity.PullRequest;

public class PullRequestPendingApproval extends PullRequestEvent {

	public PullRequestPendingApproval(PullRequest request) {
		super(request);
	}

}
