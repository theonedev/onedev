package io.onedev.server.event.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="there are successful verifications")
public class PullRequestVerificationSucceeded extends PullRequestVerificationEvent {

	public PullRequestVerificationSucceeded(PullRequest request) {
		super(request);
	}

}
