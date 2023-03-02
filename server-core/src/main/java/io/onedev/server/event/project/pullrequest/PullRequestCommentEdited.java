package io.onedev.server.event.project.pullrequest;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.security.SecurityUtils;

import java.util.Date;

public class PullRequestCommentEdited extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public PullRequestCommentEdited(PullRequestComment comment) {
		super(SecurityUtils.getUser(), new Date(), comment.getRequest());
		this.commentId = comment.getId();
	}
	
	public PullRequestComment getComment() {
		return OneDev.getInstance(PullRequestCommentManager.class).load(commentId);
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public String getActivity() {
		return "comment edited";
	}

}
