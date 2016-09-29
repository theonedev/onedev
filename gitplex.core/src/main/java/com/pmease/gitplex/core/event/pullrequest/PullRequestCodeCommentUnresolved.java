package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="Unresolved code comment")
public class PullRequestCodeCommentUnresolved extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentUnresolved(PullRequest request, CodeCommentStatusChange statusChange) {
		super(request, statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}
	
}
