package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentAware;

public class PullRequestReviewerAddData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String reviewer;
	
	public PullRequestReviewerAddData(String reviewer) {
		this.reviewer = reviewer;
	}
	
	@Override
	public String getActivity() {
		return "added reviewer \"" + reviewer + "\"";
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return null;
	}

}
