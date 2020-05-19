package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class PullRequestCodeCommentEvent extends PullRequestEvent implements MarkdownAware {

	private final CodeComment comment;
	
	public PullRequestCodeCommentEvent(User user, Date date, PullRequest request, CodeComment comment) {
		super(user, date, request);
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

}
