package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.util.CommentAware;

public class PullRequestAssigneeRemoveData extends PullRequestChangeData {

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

}
