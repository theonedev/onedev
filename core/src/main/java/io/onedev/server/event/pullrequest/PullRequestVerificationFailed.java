package io.onedev.server.event.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="there are failed verifications")
public class PullRequestVerificationFailed extends PullRequestVerificationEvent {

	public PullRequestVerificationFailed(PullRequest request) {
		super(request);
	}

}
