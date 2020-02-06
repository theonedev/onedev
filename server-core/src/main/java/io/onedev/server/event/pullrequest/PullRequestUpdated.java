package io.onedev.server.event.pullrequest;

import io.onedev.server.model.PullRequestUpdate;

public class PullRequestUpdated extends PullRequestEvent {

	private final PullRequestUpdate update;
	
	public PullRequestUpdated(PullRequestUpdate update) {
		super(null, update.getDate(), update.getRequest());
		this.update = update;
	}

	public PullRequestUpdate getUpdate() {
		return update;
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "Commits added";
		if (withEntity)
			activity += " in pull request " + getRequest().describe();
		return activity;
	}

}
