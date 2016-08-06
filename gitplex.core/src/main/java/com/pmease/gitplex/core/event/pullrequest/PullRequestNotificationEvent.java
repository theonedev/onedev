package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.PullRequest;

public abstract class PullRequestNotificationEvent extends PullRequestEvent {

	public PullRequestNotificationEvent(PullRequest request) {
		super(request);
	}

}
