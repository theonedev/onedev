package com.gitplex.server.event.pullrequest;

import java.util.Date;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.PullRequestUpdate;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="has new commits")
public class PullRequestUpdated extends PullRequestChangeEvent {

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
