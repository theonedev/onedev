package com.pmease.gitplex.core.event.codecomment;

import com.pmease.gitplex.core.entity.CodeCommentReply;

public class CodeCommentReplied extends CodeCommentEvent {

	private final CodeCommentReply reply;
	
	public CodeCommentReplied(CodeCommentReply reply) {
		super(reply.getComment());
		this.reply = reply;
	}

	public CodeCommentReply getReply() {
		return reply;
	}

	@Override
	public String getDescription() {
		return "replied";
	}
	
}
