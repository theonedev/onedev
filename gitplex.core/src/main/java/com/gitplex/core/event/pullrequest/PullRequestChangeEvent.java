package com.gitplex.core.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.PullRequest;

public abstract class PullRequestChangeEvent extends PullRequestEvent {

	public PullRequestChangeEvent(PullRequest request) {
		super(request);
	}

	@Nullable
	public abstract Account getUser();

	public abstract Date getDate();

}
