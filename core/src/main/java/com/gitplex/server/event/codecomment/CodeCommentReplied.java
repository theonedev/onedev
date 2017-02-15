package com.gitplex.server.event.codecomment;

import com.gitplex.server.entity.CodeCommentReply;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.event.pullrequest.PullRequestCodeCommentReplied;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="replied")
public class CodeCommentReplied extends CodeCommentActivityEvent {

	public CodeCommentReplied(CodeCommentReply reply) {
		super(reply);
	}

	public CodeCommentReply getReply() {
		return (CodeCommentReply) getActivity();
	}

	@Override
	public PullRequestCodeCommentActivityEvent getPullRequestCodeCommentActivityEvent(PullRequest request) {
		return new PullRequestCodeCommentReplied(request, getReply());
	}

}
