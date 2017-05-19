package com.gitplex.server.event.pullrequest;

import com.gitplex.server.event.MarkdownAware;
import com.gitplex.server.model.CodeComment;

public abstract class PullRequestCodeCommentEvent extends PullRequestEvent implements MarkdownAware {

	private final CodeComment comment;
	
	public PullRequestCodeCommentEvent(CodeComment comment) {
		super(comment.getRequest());
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

}
