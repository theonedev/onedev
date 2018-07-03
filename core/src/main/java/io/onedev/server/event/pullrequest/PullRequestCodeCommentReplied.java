package io.onedev.server.event.pullrequest;

import java.util.Date;

import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public class PullRequestCodeCommentReplied extends PullRequestCodeCommentEvent {

	private final CodeCommentReply reply;
	
	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply, boolean derived) {
		super(request, reply.getComment(), derived);
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
