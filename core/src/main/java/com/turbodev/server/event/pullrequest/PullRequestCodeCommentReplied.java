package com.turbodev.server.event.pullrequest;

import java.util.Date;

import com.turbodev.server.model.CodeCommentReply;
import com.turbodev.server.model.PullRequest;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;

@Editable(name="replied code comment")
public class PullRequestCodeCommentReplied extends PullRequestCodeCommentEvent {

	private final CodeCommentReply reply;
	
	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply, boolean passive) {
		super(request, reply.getComment(), passive);
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
