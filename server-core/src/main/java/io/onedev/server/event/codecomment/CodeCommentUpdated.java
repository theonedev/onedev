package io.onedev.server.event.codecomment;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;

public class CodeCommentUpdated extends CodeCommentEvent implements MarkdownAware {

	public CodeCommentUpdated(User user, CodeComment comment) {
		super(user, new Date(), comment);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "updated comment";
		if (withEntity)
			activity += " on file " + getComment().getMark().getPath();
		return activity;
	}

}
