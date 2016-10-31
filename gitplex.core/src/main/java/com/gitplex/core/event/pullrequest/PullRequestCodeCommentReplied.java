package com.gitplex.core.event.pullrequest;

import com.gitplex.core.entity.CodeCommentReply;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="replied code comment")
public class PullRequestCodeCommentReplied extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply) {
		super(request, reply);
	}

	public CodeCommentReply getReply() {
		return (CodeCommentReply) getActivity();
	}
	
}
