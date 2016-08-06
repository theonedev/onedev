package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.PullRequest;

public class PullRequestPendingApproval extends PullRequestNotificationEvent {

	public PullRequestPendingApproval(PullRequest request) {
		super(request);
	}

}
