package io.onedev.server.event.codecomment;

import java.util.Date;

import javax.annotation.Nullable;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class CodeCommentEvent implements MarkdownAware {

	private final CodeComment comment;
	
	private final PullRequest request;
	
	/**
	 * @param comment
	 * @param user
	 * @param date
	 * @param request
	 * 			pull request context when this event is created
	 */
	public CodeCommentEvent(CodeComment comment, @Nullable PullRequest request) {
		this.comment = comment;
		this.request = request;
	}

	public CodeComment getComment() {
		return comment;
	}

	@Nullable
	public PullRequest getRequest() {
		return request;
	}

	public abstract User getUser();
	
	public abstract Date getDate();
	
}
