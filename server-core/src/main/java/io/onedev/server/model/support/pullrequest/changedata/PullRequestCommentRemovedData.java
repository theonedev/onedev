package io.onedev.server.model.support.pullrequest.changedata;

public class PullRequestCommentRemovedData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getActivity() {
		return "removed comment";
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
}