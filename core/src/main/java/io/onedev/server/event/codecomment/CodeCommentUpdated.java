package io.onedev.server.event.codecomment;

import java.util.Date;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;

public class CodeCommentUpdated extends CodeCommentEvent implements MarkdownAware {

	public CodeCommentUpdated(User user, CodeComment comment) {
		super(user, new Date(), comment, null);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

}
