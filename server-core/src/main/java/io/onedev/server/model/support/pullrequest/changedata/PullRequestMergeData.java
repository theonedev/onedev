package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentAware;

public class PullRequestMergeData implements PullRequestChangeData {

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

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return null;
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
	}

}
