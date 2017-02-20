package com.gitplex.server.event.codecomment;

import com.gitplex.server.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentUnresolved;
import com.gitplex.server.model.CodeCommentStatusChange;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

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
 