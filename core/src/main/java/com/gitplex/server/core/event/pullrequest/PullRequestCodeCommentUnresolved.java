package com.gitplex.server.core.event.pullrequest;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.entity.CodeCommentStatusChange;
import com.gitplex.server.core.entity.PullRequest;

@Editable(name="Unresolved code comment")
public class PullRequestCodeCommentUnresolved extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentUnresolved(PullRequest request, CodeCommentStatusChange statusChange) {
		super(request, statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}
	
}
