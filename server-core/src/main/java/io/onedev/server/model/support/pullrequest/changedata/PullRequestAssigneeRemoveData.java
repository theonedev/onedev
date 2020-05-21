package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;

public class PullRequestAssigneeRemoveData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String assignee;
	
	public PullRequestAssigneeRemoveData(String assignee) {
		this.assignee = assignee;
	}
	
	@Override
	public String getActivity(PullRequest withRequest) {
		String activity = "removed assignee \"" + assignee + "\"";
		if (withRequest != null)
			activity += " in pull request " + withRequest.describe();
		return activity;
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return null;
	}

}
