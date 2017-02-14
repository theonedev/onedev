package com.gitplex.server.core.event.pullrequest;

import com.gitplex.server.core.entity.CodeComment;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.event.MarkdownAware;

public abstract class PullRequestCodeCommentEvent extends PullRequestChangeEvent implements MarkdownAware {

	private final CodeComment comment;
	
	public PullRequestCodeCommentEvent(PullRequest request, CodeComment comment) {
		super(request);
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

}
