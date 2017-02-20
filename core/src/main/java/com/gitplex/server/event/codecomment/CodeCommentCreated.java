package com.gitplex.server.event.codecomment;

import java.util.Date;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.util.editable.annotation.Editable;

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
