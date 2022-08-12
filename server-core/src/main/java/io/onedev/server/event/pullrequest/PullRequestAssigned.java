package io.onedev.server.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;

public class PullRequestAssigned extends PullRequestEvent {

	private final User assignee;
	
	public PullRequestAssigned(User user, Date date, PullRequest request, User assignee) {
		super(user, date, request);
		this.assignee = assignee;
	}
	
	@Override
	public String getActivity() {
		return "assigned to \"" + assignee.getDisplayName() + "\"";
	}

	@Nullable
	public User getAssignee() {
		return assignee;
	}

	@Override
	public PullRequestEvent cloneIn(Dao dao) {
		return new PullRequestAssigned(
				dao.load(User.class, getUser().getId()),
				getDate(),
				dao.load(PullRequest.class, getRequest().getId()), 
				dao.load(User.class, assignee.getId()));
	}
	
}
