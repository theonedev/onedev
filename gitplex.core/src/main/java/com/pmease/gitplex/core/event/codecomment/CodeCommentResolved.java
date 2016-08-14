package com.pmease.gitplex.core.event.codecomment;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;

@Editable(name="resolved")
public class CodeCommentResolved extends CodeCommentEvent {

	private final CodeCommentStatusChange statusChange;
	
	public CodeCommentResolved(CodeCommentStatusChange statusChange) {
		super(statusChange.getComment(), statusChange.getUser(), statusChange.getDate());
		this.statusChange = statusChange;
	}

	public CodeCommentStatusChange getStatusChange() {
		return statusChange;
	}

}
