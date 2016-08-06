package com.pmease.gitplex.core.event.codecomment;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;

@Editable(name="resolved")
public class CodeCommentResolved extends CodeCommentEvent {

	private final Account user;
	
	public CodeCommentResolved(CodeComment comment, Account user) {
		super(comment);
		this.user = user;
	}

	public Account getUser() {
		return user;
	}

}
