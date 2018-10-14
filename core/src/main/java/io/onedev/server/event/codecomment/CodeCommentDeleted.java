package io.onedev.server.event.codecomment;

import java.util.Date;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;

public class CodeCommentDeleted extends CodeCommentEvent {

	public CodeCommentDeleted(User user, CodeComment comment) {
		super(user, new Date(), comment, null);
	}

}
