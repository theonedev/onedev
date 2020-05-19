package io.onedev.server.event.codecomment;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;

public class CodeCommentCreated extends CodeCommentEvent implements MarkdownAware {

	public CodeCommentCreated(CodeComment comment) {
		super(comment.getUser(), comment.getCreateDate(), comment);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "commented";
		if (withEntity)
			activity += " on file " + getComment().getMark().getPath();
		return activity;
	}

}
