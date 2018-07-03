package io.onedev.server.model.support.pullrequest.actiondata;

import io.onedev.server.model.support.CommentSupport;

public class RemovedReviewerData extends ActionData {

	private static final long serialVersionUID = 1L;

	private final String reviewer;
	
	public RemovedReviewerData(String reviewer) {
		this.reviewer = reviewer;
	}
	
	@Override
	public String getDescription() {
		return "removed reviewer \"" + reviewer + "\"";
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}

}
