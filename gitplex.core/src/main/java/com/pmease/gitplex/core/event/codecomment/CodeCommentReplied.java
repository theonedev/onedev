package com.pmease.gitplex.core.event.codecomment;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.PullRequest;

@Editable(name="replied")
public class CodeCommentReplied extends CodeCommentEvent {

	private final CodeCommentReply reply;
	
	public CodeCommentReplied(CodeCommentReply reply, PullRequest request) {
		super(reply.getComment(), reply.getUser(), reply.getDate(), request);
		this.reply = reply;
	}

	public CodeCommentReply getReply() {
		return reply;
	}

	@Override
	public String getMarkdown() {
		return reply.getContent();
	}

}
