package com.pmease.gitplex.core.event.codecomment;

import com.pmease.gitplex.core.entity.CodeComment;

public class CodeCommentCreated extends CodeCommentEvent {

	public CodeCommentCreated(CodeComment comment) {
		super(comment);
	}

	@Override
	public String getDescription() {
		return "created";
	}

}
