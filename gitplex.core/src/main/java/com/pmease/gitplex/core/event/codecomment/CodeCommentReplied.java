package com.pmease.gitplex.core.event.codecomment;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentActivityEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCodeCommentReplied;

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
