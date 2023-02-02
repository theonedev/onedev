package io.onedev.server.event.project.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;

import java.util.Date;

public class IssueCommentDeleted extends IssueEvent {

	private final Long commentId;
	
	private static final long serialVersionUID = 1L;
	
	public IssueCommentDeleted(IssueComment comment) {
		super(SecurityUtils.getUser(), new Date(), comment.getIssue());
		this.commentId = comment.getId();
	}
	
	public Long getCommentId() {
		return commentId;
	}
	
	@Override
	public boolean affectsListing() {
		return false;
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
