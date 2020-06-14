package io.onedev.server.event.pullrequest;

import java.util.Collection;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;

public class PullRequestUpdated extends PullRequestEvent {

	private final PullRequestUpdate update;
	
	private transient Collection<User> committers;
	
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
			activity += " in pull request " + getRequest().getNumberAndTitle();
		return activity;
	}

	public Collection<User> getCommitters() {
		if (committers == null) {
			committers = getUpdate().getCommits()
				.stream()
				.map(it->OneDev.getInstance(UserManager.class).find(it.getCommitterIdent()))
				.filter(it->it!=null)
				.collect(Collectors.toSet());
		}
		return committers;
	}
}
