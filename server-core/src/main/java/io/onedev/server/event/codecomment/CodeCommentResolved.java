package io.onedev.server.event.codecomment;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeCommentStatusChange;

public class CodeCommentResolved extends CodeCommentStatusChanged {

	public CodeCommentResolved(CodeCommentStatusChange change, @Nullable String note) {
		super(change, note);
	}

}
