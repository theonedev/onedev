package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class PullRequestEvent extends ProjectEvent {

	private final PullRequest request;
	
	public PullRequestEvent(User user, Date date, PullRequest request) {
		super(user, date, request.getTargetProject());
		this.request = request;
	}

	public PullRequest getRequest() {
		return request;
	}

}
