package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="there are successful verifications")
public class PullRequestVerificationSucceeded extends PullRequestVerificationEvent {

	public PullRequestVerificationSucceeded(PullRequest request) {
		super(request);
	}

}
