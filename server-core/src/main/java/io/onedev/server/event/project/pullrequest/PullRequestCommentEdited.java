package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.security.SecurityUtils;

public class PullRequestCommentEdited extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public PullRequestCommentEdited(PullRequestComment comment) {
		super(SecurityUtils.getUser(), new Date(), comment.getRequest());
		this.commentId = comment.getId();
	}
	
	public PullRequestComment getComment() {
		return OneDev.getInstance(PullRequestCommentService.class).load(commentId);
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
