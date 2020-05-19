package io.onedev.server.event.codecomment;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeCommentReply;

public class CodeCommentReplied extends CodeCommentEvent implements MarkdownAware {

	private final CodeCommentReply reply;
	
	public CodeCommentReplied(CodeCommentReply reply) {
		super(reply.getUser(), reply.getDate(), reply.getComment());
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
			activity += " on file " + getComment().getMark().getPath();
		return activity;
	}

}
