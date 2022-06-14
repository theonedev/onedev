package io.onedev.server.event.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestUnassigned extends PullRequestEvent {

	private final User assignee;
	
	public PullRequestUnassigned(User user, Date date, PullRequest request, User assignee) {
		super(user, date, request);
		this.assignee = assignee;
	}
	
	@Override
	public String getActivity() {
		return "unassigned from \"" + assignee.getDisplayName() + "\"";
	}
	
	@Nullable
	public User getAssignee() {
		return assignee;
	}

}
