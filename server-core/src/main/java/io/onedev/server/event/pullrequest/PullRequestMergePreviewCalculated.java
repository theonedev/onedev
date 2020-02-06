package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequest;

public class PullRequestMergePreviewCalculated extends PullRequestEvent {
	
	public PullRequestMergePreviewCalculated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "Merge preview calculated";
		if (withEntity)
			activity += " in pull request " + getRequest().describe();
		return activity;
	}

}
