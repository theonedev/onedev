package io.onedev.server.event.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="there are verification errors")
public class PullRequestVerificationInError extends PullRequestVerificationEvent {

	public PullRequestVerificationInError(PullRequest request) {
		super(request);
	}

}
