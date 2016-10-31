package com.gitplex.core.event.pullrequest;

import com.gitplex.core.entity.PullRequest;

public class PullRequestPendingApproval extends PullRequestEvent {

	public PullRequestPendingApproval(PullRequest request) {
		super(request);
	}

}
