package io.onedev.server.event.codecomment;

import io.onedev.server.model.CodeComment;

public class CodeCommentCreated extends CodeCommentEvent {

	public CodeCommentCreated(CodeComment comment) {
		super(comment.getUser(), comment.getCreateDate(), comment);
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity() {
		return "added";
	}

}
