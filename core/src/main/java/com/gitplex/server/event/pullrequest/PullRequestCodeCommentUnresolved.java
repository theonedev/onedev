package com.gitplex.server.event.pullrequest;

import com.gitplex.server.entity.CodeCommentStatusChange;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="Unresolved code comment")
public class PullRequestCodeCommentUnresolved extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentUnresolved(PullRequest request, CodeCommentStatusChange statusChange) {
		super(request, statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}
	
}
