package io.onedev.server.model.support.pullrequest.changedata;

import java.io.Serial;

public class PullRequestApproveData extends PullRequestChangeData {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public String getActivity() {
		return "approved";
	}

}
