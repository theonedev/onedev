package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;

public class PullRequestAssigneeRemoveData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final Long assigneeId;
	
	public PullRequestAssigneeRemoveData(User assignee) {
		this.assigneeId = assignee.getId();
	}
	
	@Override
	public String getActivity() {
		User user = getUser();
		if (user != null)
			return "removed assignee \"" + user.getDisplayName() + "\"";
		else
			return "removed assignee unknown";
	}
	
	@Nullable
	public User getUser() {
		return OneDev.getInstance(UserManager.class).get(assigneeId);
	}

}
