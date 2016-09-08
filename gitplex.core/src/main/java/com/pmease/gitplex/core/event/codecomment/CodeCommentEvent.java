package com.pmease.gitplex.core.event.codecomment;

import java.util.Date;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.MarkdownAware;

public abstract class CodeCommentEvent implements MarkdownAware {

	private final CodeComment comment;
	
	private final Account user;
	
	private final Date date;
	
	private final PullRequest request;
	
	/**
	 * @param comment
	 * @param user
	 * @param date
	 * @param request
	 * 			pull request context when this event is created
	 */
	public CodeCommentEvent(CodeComment comment, Account user, Date date, @Nullable PullRequest request) {
		this.comment = comment;
		this.user = user;
		this.date = date;
		this.request = request;
	}

	public CodeComment getComment() {
		return comment;
	}

	public Account getUser() {
		return user;
	}

	public Date getDate() {
		return date;
	}

	@Nullable
	public PullRequest getRequest() {
		return request;
	}
	
}
