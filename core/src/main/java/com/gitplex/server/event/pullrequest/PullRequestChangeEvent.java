package com.gitplex.server.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;

public abstract class PullRequestChangeEvent extends PullRequestEvent {

	public PullRequestChangeEvent(PullRequest request) {
		super(request);
	}

	@Nullable
	public abstract Account getUser();

	public abstract Date getDate();

}
