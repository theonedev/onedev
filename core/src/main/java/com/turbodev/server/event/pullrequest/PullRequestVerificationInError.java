package com.turbodev.server.event.pullrequest;

import com.turbodev.server.model.PullRequest;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="there are verification errors")
public class PullRequestVerificationInError extends PullRequestVerificationEvent {

	public PullRequestVerificationInError(PullRequest request) {
		super(request);
	}

}
