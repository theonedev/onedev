package com.gitplex.core.event.pullrequest;

import com.gitplex.core.entity.CodeCommentStatusChange;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="Unresolved code comment")
public class PullRequestCodeCommentUnresolved extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentUnresolved(PullRequest request, CodeCommentStatusChange statusChange) {
		super(request, statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}
	
}
