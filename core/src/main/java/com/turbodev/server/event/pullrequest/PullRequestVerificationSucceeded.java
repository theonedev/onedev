package com.turbodev.server.event.pullrequest;

import com.turbodev.server.model.PullRequest;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="there are successful verifications")
public class PullRequestVerificationSucceeded extends PullRequestVerificationEvent {

	public PullRequestVerificationSucceeded(PullRequest request) {
		super(request);
	}

}
