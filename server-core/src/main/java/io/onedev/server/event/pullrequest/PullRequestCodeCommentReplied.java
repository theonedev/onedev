package io.onedev.server.event.pullrequest;

import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;

public class PullRequestCodeCommentReplied extends PullRequestCodeCommentEvent {

	private final CodeCommentReply reply;
	
	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply, boolean derived) {
		super(reply.getUser(), reply.getDate(), request, reply.getComment(), derived);
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
