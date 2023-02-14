package io.onedev.server.model.support.pullrequest.changedata;

public class PullRequestSourceBranchDeleteData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	@Override
	public String getActivity() {
		return "deleted source branch";
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
}
