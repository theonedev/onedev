package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="Unresolved code comment")
public class PullRequestCodeCommentUnresolved extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentUnresolved(CodeCommentStatusChange statusChange) {
		super(statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}
	
}
