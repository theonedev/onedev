package io.onedev.server.event.project.pullrequest;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Date;

import io.onedev.server.model.PullRequest;

public class PullRequestCheckFailed extends PullRequestEvent {
	
	private static final long serialVersionUID = 1L;

	public PullRequestCheckFailed(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	@Override
	public String getActivity() {
		return _T("check failed");
	}

}
