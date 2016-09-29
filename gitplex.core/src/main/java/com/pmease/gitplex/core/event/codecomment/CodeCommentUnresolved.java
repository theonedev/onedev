package com.pmease.gitplex.core.event.codecomment;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentStatusChange;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentUnresolved;

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
 