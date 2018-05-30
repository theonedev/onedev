package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;

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
