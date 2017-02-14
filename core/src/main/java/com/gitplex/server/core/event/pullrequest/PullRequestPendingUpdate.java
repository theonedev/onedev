package com.gitplex.server.core.event.pullrequest;

import com.gitplex.server.core.entity.PullRequest;

public class PullRequestPendingUpdate extends PullRequestEvent {

	public PullRequestPendingUpdate(PullRequest request) {
		super(request);
	}

}
