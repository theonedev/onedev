package io.onedev.server.model.support.pullrequest.changedata;

public class PullRequestRequestedForChangesData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getActivity() {
		return "requested for changes";
	}

}