package io.onedev.server.event.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="there are running verifications")
public class PullRequestVerificationRunning extends PullRequestVerificationEvent {

	public PullRequestVerificationRunning(PullRequest request) {
		super(request);
	}

}
