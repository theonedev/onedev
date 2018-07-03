package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestBuildEvent extends PullRequestEvent {

	private final Build build;
	
	private final Date date;
	
	public PullRequestBuildEvent(PullRequest request, Build build) {
		super(request);
		this.build = build;
		date = new Date();
	}

	public Build getBuild() {
		return build;
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
