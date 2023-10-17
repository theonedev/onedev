package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class PullRequestCodeCommentEvent extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public PullRequestCodeCommentEvent(User user, Date date, PullRequest request, CodeComment comment) {
		super(user, date, request);
		commentId = comment.getId();
	}

	public CodeComment getComment() {
		return OneDev.getInstance(CodeCommentManager.class).load(commentId);
	}

}
