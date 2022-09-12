package io.onedev.server.event.codecomment;

import java.util.Date;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class CodeCommentUpdated extends CodeCommentEvent {

	public CodeCommentUpdated(User user, CodeComment comment) {
		super(user, new Date(), comment);
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
	}

	@Override
	public String getActivity() {
		return "updated";
	}

	@Override
	public CodeCommentEvent cloneIn(Dao dao) {
		return new CodeCommentUpdated(
				dao.load(User.class, getUser().getId()),
				dao.load(CodeComment.class, getComment().getId()));
	}
	
}
