package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="there are running verifications")
public class PullRequestVerificationRunning extends PullRequestVerificationEvent {

	public PullRequestVerificationRunning(PullRequest request) {
		super(request);
	}

}
