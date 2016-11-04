package com.gitplex.server.core.event.codecomment;

import com.gitplex.commons.wicket.editable.annotation.Editable;
import com.gitplex.server.core.entity.CodeCommentReply;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.server.core.event.pullrequest.PullRequestCodeCommentReplied;

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
