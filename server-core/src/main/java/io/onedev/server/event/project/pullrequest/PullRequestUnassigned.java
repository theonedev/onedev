package io.onedev.server.event.project.pullrequest;

import java.text.MessageFormat;
import java.util.Date;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.service.UserService;
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
		return MessageFormat.format("unassigned from \"{0}\"", getAssignee().getDisplayName());
	}
	
	@Nullable
	public User getAssignee() {
		return OneDev.getInstance(UserService.class).load(assigneeId);
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
}
