package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.gitplex.core.entity.PullRequest;

public class IntegrationPreviewCalculated extends PullRequestChangeEvent {

	public IntegrationPreviewCalculated(PullRequest request) {
		super(request, null, new Date());
	}

}
