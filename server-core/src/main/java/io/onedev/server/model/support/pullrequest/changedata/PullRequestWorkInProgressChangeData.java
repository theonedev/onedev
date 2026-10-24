package io.onedev.server.model.support.pullrequest.changedata;

import java.io.Serial;

public class PullRequestWorkInProgressChangeData extends PullRequestChangeData {

	@Serial
	private static final long serialVersionUID = 1L;

	private final boolean workInProgress;

	public PullRequestWorkInProgressChangeData(boolean workInProgress) {
		this.workInProgress = workInProgress;
	}

	@Override
	public String getActivity() {
		return workInProgress ? "marked as WIP" : "removed WIP mark";
	}

}
