package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestMergePreviewCalculated extends PullRequestEvent {
	
	private final Date date;
	
	public PullRequestMergePreviewCalculated(PullRequest request) {
		super(request);
		date = new Date();
	}

	@Override
	public User getUser() {
		return null;
	}

	@Override
	public Date getDate() {
		return date;
	}

}
