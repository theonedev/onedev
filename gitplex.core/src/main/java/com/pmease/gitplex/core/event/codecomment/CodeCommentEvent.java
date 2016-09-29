package com.pmease.gitplex.core.event.codecomment;

import java.util.Date;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.event.MarkdownAware;

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

	public abstract Account getUser();
	
	public abstract Date getDate();
	
}
