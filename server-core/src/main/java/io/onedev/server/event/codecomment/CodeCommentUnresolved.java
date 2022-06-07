package io.onedev.server.event.codecomment;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeCommentStatusChange;

public class CodeCommentUnresolved extends CodeCommentStatusChanged {

	public CodeCommentUnresolved(CodeCommentStatusChange change, @Nullable String note) {
		super(change, note);
	}

}
