package io.onedev.server.model.support.pullrequest.changedata;

public class PullRequestSourceBranchRestoreData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	@Override
	public String getActivity() {
		return "restored source branch";
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
}
