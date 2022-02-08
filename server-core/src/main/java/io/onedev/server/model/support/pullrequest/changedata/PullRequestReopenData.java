package io.onedev.server.model.support.pullrequest.changedata;

public class PullRequestReopenData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	@Override
	public String getActivity() {
		return "reopened";
	}

}
