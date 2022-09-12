package io.onedev.server.event.codecomment;

import io.onedev.server.model.CodeComment;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class CodeCommentCreated extends CodeCommentEvent {

	public CodeCommentCreated(CodeComment comment) {
		super(comment.getUser(), comment.getCreateDate(), comment);
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
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
