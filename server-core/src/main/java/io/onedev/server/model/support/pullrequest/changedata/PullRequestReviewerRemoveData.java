package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.util.CommentAware;

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

	@Override
	public CommentAware getCommentAware() {
		return null;
	}

}
