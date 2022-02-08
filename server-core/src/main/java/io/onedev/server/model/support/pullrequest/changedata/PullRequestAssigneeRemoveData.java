package io.onedev.server.model.support.pullrequest.changedata;

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

}
