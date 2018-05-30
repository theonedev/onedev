package io.onedev.server.event.codecomment;

import java.util.Date;

import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;

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
