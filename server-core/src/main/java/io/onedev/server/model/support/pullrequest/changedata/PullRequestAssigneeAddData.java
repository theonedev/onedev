package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.util.CommentAware;

public class PullRequestAssigneeAddData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String assignee;
	
	public PullRequestAssigneeAddData(String assignee) {
		this.assignee = assignee;
	}
	
	@Override
	public String getActivity() {
		return "added assignee \"" + assignee + "\"";
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
	}

}
