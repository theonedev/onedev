package io.onedev.server.model.support.pullrequest.actiondata;

import javax.annotation.Nullable;

public class MergedData extends ActionData {

	private static final long serialVersionUID = 1L;

	private final String reason;
	
	public MergedData(@Nullable String reason) {
		this.reason = reason;
	}
	
	@Override
	public String getDescription() {
		if (reason != null)
			return reason;
		else
			return "merged pull request";
	}

}
