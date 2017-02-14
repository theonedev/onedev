package com.gitplex.server.core.event.pullrequest;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.entity.CodeCommentStatusChange;
import com.gitplex.server.core.entity.PullRequest;

@Editable(name="resolved code comment")
public class PullRequestCodeCommentResolved extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentResolved(PullRequest request, CodeCommentStatusChange statusChange) {
		super(request, statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}
	
}
