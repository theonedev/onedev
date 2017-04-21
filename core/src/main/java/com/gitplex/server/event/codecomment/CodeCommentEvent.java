package com.gitplex.server.event.codecomment;

import java.util.Date;

import javax.annotation.Nullable;

import com.gitplex.server.event.MarkdownAware;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;

public abstract class CodeCommentEvent implements MarkdownAware {

	private final CodeComment comment;
	
	/**
	 * @param comment
	 * @param user
	 * @param date
	 * @param request
	 * 			pull request context when this event is created
	 */
	public CodeCommentEvent(CodeComment comment) {
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

	@Nullable
	public abstract Account getUser();
	
	public abstract Date getDate();
	
}
