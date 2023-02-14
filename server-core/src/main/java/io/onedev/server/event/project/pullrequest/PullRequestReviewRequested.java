package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestReviewRequested extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long reviewerId;
	
	public PullRequestReviewRequested(User user, Date date, PullRequest request, User reviewer) {
		super(user, date, request);
		reviewerId = reviewer.getId();
	}
	
	@Override
	public String getActivity() {
		return "requested review from " + getReviewer().getDisplayName();
	}

	public User getReviewer() {
		return OneDev.getInstance(UserManager.class).load(reviewerId);
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
}
