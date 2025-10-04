package io.onedev.server.event.project.pullrequest;

import java.text.MessageFormat;
import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.service.UserService;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestReviewerRemoved extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long reviewerId;
	
	public PullRequestReviewerRemoved(User user, Date date, PullRequest request, User reviewer) {
		super(user, date, request);
		reviewerId = reviewer.getId();
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	@Override
	public String getActivity() {
		return MessageFormat.format("removed reviewer \"{0}\"", getReviewer().getDisplayName());
	}

	public User getReviewer() {
		return OneDev.getInstance(UserService.class).load(reviewerId);
	}

}
