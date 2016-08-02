package com.pmease.gitplex.core.event.codecomment;

import com.pmease.gitplex.core.entity.CodeComment;

public abstract class CodeCommentEvent {

	private final CodeComment comment;
	
	public CodeCommentEvent(CodeComment comment) {
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

	public abstract String getDescription();
	
}
