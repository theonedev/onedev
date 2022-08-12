package io.onedev.server.event.codecomment;

import io.onedev.server.model.CodeComment;
import io.onedev.server.persistence.dao.Dao;

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

	@Override
	public CodeCommentEvent cloneIn(Dao dao) {
		return new CodeCommentCreated(dao.load(CodeComment.class, getComment().getId()));
	}

}
