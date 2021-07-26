package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentAware;

public class PullRequestAssigneeRemoveData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String assignee;
	
	public PullRequestAssigneeRemoveData(String assignee) {
		this.assignee = assignee;
	}
	
	@Override
	public String getActivity() {
		return "removed assignee \"" + assignee + "\"";
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return null;
	}

}
