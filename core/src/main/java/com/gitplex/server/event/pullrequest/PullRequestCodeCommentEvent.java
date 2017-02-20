package com.gitplex.server.event.pullrequest;

import com.gitplex.server.event.MarkdownAware;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.PullRequest;

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
