package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;

public class PullRequestReviewerAddData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String reviewer;
	
	public PullRequestReviewerAddData(String reviewer) {
		this.reviewer = reviewer;
	}
	
	@Override
	public String getDescription() {
		return "added reviewer \"" + reviewer + "\"";
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return null;
	}

}
