package com.gitplex.server.event.pullrequest;

import com.gitplex.server.event.MarkdownAware;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.PullRequest;

public abstract class PullRequestCodeCommentEvent extends PullRequestEvent implements MarkdownAware {

	private final CodeComment comment;
	
	private final boolean passive;
	
	public PullRequestCodeCommentEvent(PullRequest request, CodeComment comment, boolean passive) {
		super(request);
		this.comment = comment;
		this.passive = passive;
	}

	public CodeComment getComment() {
		return comment;
	}

	public boolean isPassive() {
		return passive;
	}

}
