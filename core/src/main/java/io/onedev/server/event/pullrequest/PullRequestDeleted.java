package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestDeleted extends PullRequestEvent {

	public PullRequestDeleted(User user, PullRequest request) {
		super(user, new Date(), request);
	}

}
