package io.onedev.server.event.project.pullrequest;

import io.onedev.server.model.PullRequest;

import java.util.Date;

public class PullRequestMergePreviewUpdated extends PullRequestEvent {
	
	private static final long serialVersionUID = 1L;

	public PullRequestMergePreviewUpdated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public String getActivity() {
		return "Merge preview updated";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

}
