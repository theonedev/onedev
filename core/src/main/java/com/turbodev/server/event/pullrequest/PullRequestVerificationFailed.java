package com.turbodev.server.event.pullrequest;

import com.turbodev.server.model.PullRequest;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="there are failed verifications")
public class PullRequestVerificationFailed extends PullRequestVerificationEvent {

	public PullRequestVerificationFailed(PullRequest request) {
		super(request);
	}

}
