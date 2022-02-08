package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

public class PullRequestMergeData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String reason;
	
	public PullRequestMergeData(@Nullable String reason) {
		this.reason = reason;
	}
	
	@Override
	public String getActivity() {
		if (reason != null) 
			return reason;
		else 
			return "merged";
	}

}
