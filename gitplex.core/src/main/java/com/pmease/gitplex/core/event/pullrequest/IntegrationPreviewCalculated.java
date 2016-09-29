package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

public class IntegrationPreviewCalculated extends PullRequestChangeEvent {

	private Date date;
	
	public IntegrationPreviewCalculated(PullRequest request) {
		super(request);
		date = new Date();
	}

	@Override
	public Account getUser() {
		return null;
	}

	@Override
	public Date getDate() {
		return date;
	}

}
