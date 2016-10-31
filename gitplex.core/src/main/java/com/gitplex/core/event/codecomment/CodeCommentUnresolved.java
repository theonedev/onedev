package com.gitplex.core.event.codecomment;

import com.gitplex.core.entity.CodeCommentStatusChange;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.core.event.pullrequest.PullRequestCodeCommentUnresolved;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="unresolved")
public class CodeCommentUnresolved extends CodeCommentActivityEvent {

	public CodeCommentUnresolved(CodeCommentStatusChange statusChange) {
		super(statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}
	
	@Override
	public PullRequestCodeCommentActivityEvent getPullRequestCodeCommentActivityEvent(PullRequest request) {
		return new PullRequestCodeCommentUnresolved(request, getStatusChange());
	}
	
}
 