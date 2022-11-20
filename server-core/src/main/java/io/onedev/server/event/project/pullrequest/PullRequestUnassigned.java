package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestUnassigned extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long assigneeId;
	
	public PullRequestUnassigned(User user, Date date, PullRequest request, User assignee) {
		super(user, date, request);
		assigneeId = assignee.getId();
	}
	
	@Override
	public String getActivity() {
		return "unassigned from \"" + getAssignee().getDisplayName() + "\"";
	}
	
	@Nullable
	public User getAssignee() {
		return OneDev.getInstance(UserManager.class).load(assigneeId);
	}

}
