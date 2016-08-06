package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.gitplex.core.entity.PullRequest;

public class IntegrationPreviewCalculated extends PullRequestChangeEvent {

	public IntegrationPreviewCalculated(PullRequest request) {
		super(request);
	}

}
