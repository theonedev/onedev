package com.gitplex.server.event.pullrequest;

import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="resolved code comment")
public class PullRequestCodeCommentResolved extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentResolved(PullRequest request, CodeCommentStatusChange statusChange) {
		super(request, statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}
	
}
