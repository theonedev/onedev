package com.gitplex.server.event.pullrequest;

import com.gitplex.server.entity.CodeCommentReply;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="replied code comment")
public class PullRequestCodeCommentReplied extends PullRequestCodeCommentActivityEvent {

	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply) {
		super(request, reply);
	}

	public CodeCommentReply getReply() {
		return (CodeCommentReply) getActivity();
	}
	
}
