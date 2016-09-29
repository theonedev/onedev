package com.pmease.gitplex.core.event.codecomment;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentResolved;

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
