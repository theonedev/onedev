package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.PullRequest;

public abstract class PullRequestChangeEvent extends PullRequestEvent {

	public PullRequestChangeEvent(PullRequest request) {
		super(request);
	}

}
