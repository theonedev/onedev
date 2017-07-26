package com.gitplex.server.event.codecomment;

import java.util.Date;

import com.gitplex.server.model.CodeCommentReply;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.User;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="replied")
public class CodeCommentReplied extends CodeCommentEvent {

	private final CodeCommentReply reply;
	
	public CodeCommentReplied(CodeCommentReply reply, PullRequest request) {
		super(reply.getComment(), request);
		this.reply = reply;
	}

	public CodeCommentReply getReply() {
		return reply;
	}

	@Override
	public String getMarkdown() {
		return reply.getContent();
	}

	@Override
	public User getUser() {
		return reply.getUser();
	}

	@Override
	public Date getDate() {
		return reply.getDate();
	}

}
