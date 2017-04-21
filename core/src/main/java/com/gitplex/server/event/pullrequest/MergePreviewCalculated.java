package com.gitplex.server.event.pullrequest;

import java.util.Date;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;

public class MergePreviewCalculated extends PullRequestEvent {

	private Date date;
	
	public MergePreviewCalculated(PullRequest request) {
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
