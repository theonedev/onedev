package io.onedev.server.event.project.codecomment;

import java.util.Date;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.MarkdownText;

public class CodeCommentEdited extends CodeCommentEvent {

	private static final long serialVersionUID = 1L;

	public CodeCommentEdited(User user, CodeComment comment) {
		super(user, new Date(), comment);
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
	}

	@Override
	public String getActivity() {
		return "edited";
	}

}
