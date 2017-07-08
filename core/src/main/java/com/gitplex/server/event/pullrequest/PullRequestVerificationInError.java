package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="there are verification errors in")
public class PullRequestVerificationInError extends PullRequestVerificationEvent {

	public PullRequestVerificationInError(PullRequest request) {
		super(request);
	}

}
