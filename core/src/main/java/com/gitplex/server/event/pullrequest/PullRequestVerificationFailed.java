package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="there are failed verifications")
public class PullRequestVerificationFailed extends PullRequestVerificationEvent {

	public PullRequestVerificationFailed(PullRequest request) {
		super(request);
	}

}
