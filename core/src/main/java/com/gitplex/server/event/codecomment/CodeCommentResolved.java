package com.gitplex.server.event.codecomment;

import com.gitplex.server.entity.CodeCommentStatusChange;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentResolved;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="resolved")
public class CodeCommentResolved extends CodeCommentActivityEvent {

	public CodeCommentResolved(CodeCommentStatusChange statusChange) {
		super(statusChange);
	}

	public CodeCommentStatusChange getStatusChange() {
		return (CodeCommentStatusChange) getActivity();
	}

	@Override
	public PullRequestCodeCommentActivityEvent getPullRequestCodeCommentActivityEvent(PullRequest request) {
		return new PullRequestCodeCommentResolved(request, getStatusChange());
	}
	
}
