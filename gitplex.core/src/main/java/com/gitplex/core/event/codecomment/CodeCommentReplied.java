package com.gitplex.core.event.codecomment;

import com.gitplex.core.entity.CodeCommentReply;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.gitplex.core.event.pullrequest.PullRequestCodeCommentReplied;
import com.gitplex.commons.wicket.editable.annotation.Editable;

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
