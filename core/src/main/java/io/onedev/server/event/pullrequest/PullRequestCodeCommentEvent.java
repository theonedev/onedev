package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class PullRequestCodeCommentEvent extends PullRequestEvent implements MarkdownAware {

	private final CodeComment comment;
	
	private final boolean derived;
	
	public PullRequestCodeCommentEvent(User user, Date date, PullRequest request, CodeComment comment, boolean derived) {
		super(user, date, request);
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
