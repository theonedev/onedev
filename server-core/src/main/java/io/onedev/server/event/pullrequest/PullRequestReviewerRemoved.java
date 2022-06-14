package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestReviewerRemoved extends PullRequestEvent {

	private final User reviewer;
	
	public PullRequestReviewerRemoved(User user, Date date, PullRequest request, User reviewer) {
		super(user, date, request);
		this.reviewer = reviewer;
	}
	
	@Override
	public String getActivity() {
		return "removed reviewer \"" + reviewer.getDisplayName() + "\"";
	}

	public User getReviewer() {
		return reviewer;
	}
	
}
