package com.gitplex.core.event.pullrequest;

import com.gitplex.core.entity.PullRequest;

public class PullRequestPendingUpdate extends PullRequestEvent {

	public PullRequestPendingUpdate(PullRequest request) {
		super(request);
	}

}
