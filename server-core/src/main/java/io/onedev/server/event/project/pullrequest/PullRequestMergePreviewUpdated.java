package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequest;

public class PullRequestMergePreviewUpdated extends PullRequestEvent {
	
	private static final long serialVersionUID = 1L;

	public PullRequestMergePreviewUpdated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public String getActivity() {
		return "merge preview updated";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

}
