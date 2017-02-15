package com.gitplex.server.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.PullRequest;

public abstract class PullRequestChangeEvent extends PullRequestEvent {

	public PullRequestChangeEvent(PullRequest request) {
		super(request);
	}

	@Nullable
	public abstract Account getUser();

	public abstract Date getDate();

}
