package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequest;

public class PullRequestCheckFailed extends PullRequestEvent {
	
	private static final long serialVersionUID = 1L;

	public PullRequestCheckFailed(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public String getActivity() {
		return "Check failed";
	}

}
