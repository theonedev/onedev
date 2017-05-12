package com.gitplex.server.event.pullrequest;

import java.util.Date;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="there are new commits")
public class PullRequestUpdated extends PullRequestEvent {

	private final PullRequestUpdate update;
	
	public PullRequestUpdated(PullRequestUpdate update) {
		super(update.getRequest());
		this.update = update;
	}

	public PullRequestUpdate getUpdate() {
		return update;
	}

	@Override
	public Account getUser() {
		return null;
	}

	@Override
	public Date getDate() {
		return update.getDate();
	}

}
