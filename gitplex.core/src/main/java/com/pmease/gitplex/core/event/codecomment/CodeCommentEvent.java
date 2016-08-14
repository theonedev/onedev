package com.pmease.gitplex.core.event.codecomment;

import java.util.Date;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;

public abstract class CodeCommentEvent {

	private final CodeComment comment;
	
	private final Account user;
	
	private final Date date;
	
	public CodeCommentEvent(CodeComment comment, Account user, Date date) {
		this.comment = comment;
		this.user = user;
		this.date = date;
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
	
}
