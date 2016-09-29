package com.pmease.gitplex.core.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

public abstract class PullRequestChangeEvent extends PullRequestEvent {

	public PullRequestChangeEvent(PullRequest request) {
		super(request);
	}

	@Nullable
	public abstract Account getUser();

	public abstract Date getDate();

}
