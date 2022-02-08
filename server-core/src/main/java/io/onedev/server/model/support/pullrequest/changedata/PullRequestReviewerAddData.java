package io.onedev.server.model.support.pullrequest.changedata;

public class PullRequestReviewerAddData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String reviewer;
	
	public PullRequestReviewerAddData(String reviewer) {
		this.reviewer = reviewer;
	}
	
	@Override
	public String getActivity() {
		return "added reviewer \"" + reviewer + "\"";
	}

}
