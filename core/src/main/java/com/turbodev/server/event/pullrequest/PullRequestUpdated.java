package com.turbodev.server.event.pullrequest;

import java.util.Date;

import com.turbodev.server.model.PullRequestUpdate;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

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
	public User getUser() {
		return null;
	}

	@Override
	public Date getDate() {
		return update.getDate();
	}

}
