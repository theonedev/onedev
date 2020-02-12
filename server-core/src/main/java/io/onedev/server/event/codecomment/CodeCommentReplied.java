package io.onedev.server.event.codecomment;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;

public class CodeCommentReplied extends CodeCommentEvent implements MarkdownAware {

	private final CodeCommentReply reply;
	
	public CodeCommentReplied(CodeCommentReply reply, PullRequest request) {
		super(reply.getUser(), reply.getDate(), reply.getComment(), request);
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
		String activity = "replied comment";
		if (withEntity)
			activity += " on file " + getComment().getMarkPos().getPath();
		return activity;
	}

}
