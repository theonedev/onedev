package com.pmease.gitplex.core.event.pullrequest;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="replied code comment")
public class PullRequestCodeCommentReplied extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply) {
		super(request, reply);
	}

	public CodeCommentReply getReply() {
		return (CodeCommentReply) getActivity();
	}
	
}
