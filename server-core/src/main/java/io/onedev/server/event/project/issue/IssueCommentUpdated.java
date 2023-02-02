package io.onedev.server.event.project.issue;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.model.IssueComment;
import io.onedev.server.security.SecurityUtils;

import java.util.Date;

public class IssueCommentUpdated extends IssueEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public IssueCommentUpdated(IssueComment comment) {
		super(SecurityUtils.getUser(), new Date(), comment.getIssue());
		this.commentId = comment.getId();
	}
	
	public IssueComment getComment() {
		return OneDev.getInstance(IssueCommentManager.class).load(commentId);
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
		return "comment updated";
	}

}
