package io.onedev.server.model.support.pullrequest.changedata;

public class PullRequestReviewerRemoveData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String reviewer;
	
	public PullRequestReviewerRemoveData(String reviewer) {
		this.reviewer = reviewer;
	}
	
	@Override
	public String getActivity() {
		return "removed reviewer \"" + reviewer + "\"";
	}

}
