package io.onedev.server.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class PullRequestEvent {

	private final PullRequest request;
	
	public PullRequestEvent(PullRequest request) {
		this.request = request;
	}

	public PullRequest getRequest() {
		return request;
	}
	
	@Nullable
	public abstract User getUser();

	public abstract Date getDate();
	
}
