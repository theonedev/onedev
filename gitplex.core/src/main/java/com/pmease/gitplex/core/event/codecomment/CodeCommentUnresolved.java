package com.pmease.gitplex.core.event.codecomment;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;

@Editable(name="unresolved")
public class CodeCommentUnresolved extends CodeCommentEvent {

	private final Account user;
	
	public CodeCommentUnresolved(CodeComment comment, Account user) {
		super(comment);
		this.user = user;
	}

	public Account getUser() {
		return user;
	}

}
