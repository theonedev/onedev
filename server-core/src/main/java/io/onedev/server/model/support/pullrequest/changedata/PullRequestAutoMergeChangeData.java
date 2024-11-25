package io.onedev.server.model.support.pullrequest.changedata;

public class PullRequestAutoMergeChangeData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final boolean enabled;
	
	public PullRequestAutoMergeChangeData(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public String getActivity() {
		return enabled? "enabled auto merge": "disabled auto merge";
	}
	
}
