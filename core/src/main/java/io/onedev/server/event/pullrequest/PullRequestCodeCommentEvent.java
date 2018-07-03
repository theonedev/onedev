package io.onedev.server.event.pullrequest;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;

public abstract class PullRequestCodeCommentEvent extends PullRequestEvent implements MarkdownAware {

	private final CodeComment comment;
	
	private final boolean derived;
	
	public PullRequestCodeCommentEvent(PullRequest request, CodeComment comment, boolean derived) {
		super(request);
		this.comment = comment;
		this.derived = derived;
	}

	public CodeComment getComment() {
		return comment;
	}

	public boolean isDerived() {
		return derived;
	}

}
