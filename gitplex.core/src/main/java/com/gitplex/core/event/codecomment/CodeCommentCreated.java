package com.gitplex.core.event.codecomment;

import java.util.Date;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.CodeComment;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="created")
public class CodeCommentCreated extends CodeCommentEvent {

	public CodeCommentCreated(CodeComment comment) {
		super(comment);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public Account getUser() {
		return getComment().getUser();
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

}
