package com.gitplex.core.event.codecomment;

import com.gitplex.core.entity.CodeCommentStatusChange;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.core.event.pullrequest.PullRequestCodeCommentResolved;
import com.gitplex.commons.wicket.editable.annotation.Editable;

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
