package com.pmease.gitplex.core.event.codecomment;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="unresolved")
public class CodeCommentUnresolved extends CodeCommentEvent {

	private final CodeCommentStatusChange statusChange;
	
	public CodeCommentUnresolved(CodeCommentStatusChange statusChange, PullRequest request) {
		super(statusChange.getComment(), statusChange.getUser(), statusChange.getDate(), request);
		this.statusChange = statusChange;
	}

	public CodeCommentStatusChange getStatusChange() {
		return statusChange;
	}

	@Override
	public String getMarkdown() {
		return statusChange.getNote();
	}

}
 