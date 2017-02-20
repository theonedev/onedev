package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.PullRequest;

public class PullRequestPendingUpdate extends PullRequestEvent {

	public PullRequestPendingUpdate(PullRequest request) {
		super(request);
	}

}
