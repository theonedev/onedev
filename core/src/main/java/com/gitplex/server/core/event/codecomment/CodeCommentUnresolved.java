package com.gitplex.server.core.event.codecomment;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.entity.CodeCommentStatusChange;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.core.event.pullrequest.PullRequestCodeCommentUnresolved;

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
 