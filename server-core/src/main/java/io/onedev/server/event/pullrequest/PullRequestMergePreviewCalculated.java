package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequest;

public class PullRequestMergePreviewCalculated extends PullRequestEvent {
	
	public PullRequestMergePreviewCalculated(PullRequest request) {
		super(null, new Date(), request);
	}

}
