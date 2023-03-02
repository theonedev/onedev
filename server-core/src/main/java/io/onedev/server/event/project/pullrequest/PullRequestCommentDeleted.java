package io.onedev.server.event.project.pullrequest;

import io.onedev.server.model.PullRequestComment;
import io.onedev.server.security.SecurityUtils;

import java.util.Date;

public class PullRequestCommentDeleted extends PullRequestEvent {

	private final Long commentId;
	
	private static final long serialVersionUID = 1L;
	
	public PullRequestCommentDeleted(PullRequestComment comment) {
		super(SecurityUtils.getUser(), new Date(), comment.getRequest());
		this.commentId = comment.getId();
	}
	
	public Long getCommentId() {
		return commentId;
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public String getActivity() {
		return "comment deleted";
	}

}
