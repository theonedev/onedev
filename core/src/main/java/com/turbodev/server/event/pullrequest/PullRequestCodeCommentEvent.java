package com.turbodev.server.event.pullrequest;

import com.turbodev.server.event.MarkdownAware;
import com.turbodev.server.model.CodeComment;
import com.turbodev.server.model.PullRequest;

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
