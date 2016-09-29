package com.pmease.gitplex.core.event.codecomment;

import java.util.Date;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;

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
