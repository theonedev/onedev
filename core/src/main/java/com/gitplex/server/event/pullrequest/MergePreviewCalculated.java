package com.gitplex.server.event.pullrequest;

import java.util.Date;

import com.gitplex.server.model.User;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="merge preview calculated")
public class MergePreviewCalculated extends PullRequestEvent {

	private Date date;
	
	public MergePreviewCalculated(PullRequest request) {
		super(request);
		date = new Date();
	}

	@Override
	public User getUser() {
		return null;
	}

	@Override
	public Date getDate() {
		return date;
	}

}
