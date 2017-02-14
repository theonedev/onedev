package com.gitplex.server.core.event.codecomment;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.entity.CodeCommentStatusChange;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.core.event.pullrequest.PullRequestCodeCommentResolved;

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
