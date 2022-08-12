package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;

public class PullRequestReviewRequested extends PullRequestEvent {

	private final User reviewer;
	
	public PullRequestReviewRequested(User user, Date date, PullRequest request, User reviewer) {
		super(user, date, request);
		this.reviewer = reviewer;
	}
	
	@Override
	public String getActivity() {
		return "requested review from " + getReviewer().getDisplayName();
	}

	public User getReviewer() {
		return reviewer;
	}
	
	@Override
	public PullRequestEvent cloneIn(Dao dao) {
		return new PullRequestReviewRequested(
				dao.load(User.class, getUser().getId()), 
				getDate(), 
				dao.load(PullRequest.class, getRequest().getId()), 
				dao.load(User.class, reviewer.getId()));
	}
	
}
