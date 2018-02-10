package com.turbodev.server.event.pullrequest;

import com.turbodev.server.model.PullRequest;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="there are running verifications")
public class PullRequestVerificationRunning extends PullRequestVerificationEvent {

	public PullRequestVerificationRunning(PullRequest request) {
		super(request);
	}

}
