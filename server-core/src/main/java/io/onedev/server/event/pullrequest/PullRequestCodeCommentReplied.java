package io.onedev.server.event.pullrequest;

import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;

public class PullRequestCodeCommentReplied extends PullRequestCodeCommentEvent {

	private final CodeCommentReply reply;
	
	public PullRequestCodeCommentReplied(PullRequest request, CodeCommentReply reply) {
		super(reply.getUser(), reply.getDate(), request, reply.getComment());
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
	public String getActivity(boolean withEntity) {
		String activity = "replied code comment"; 
		if (withEntity)
			activity += " in pull request " + getRequest().getNumberAndTitle();
		return activity;
	}

}
