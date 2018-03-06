package io.onedev.server.event.pullrequest;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;

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
