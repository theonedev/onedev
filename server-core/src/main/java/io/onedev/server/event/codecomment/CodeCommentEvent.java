package io.onedev.server.event.codecomment;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;

public abstract class CodeCommentEvent extends ProjectEvent {

	private final CodeComment comment;
	
	/**
	 * @param comment
	 * @param user
	 * @param date
	 */
	public CodeCommentEvent(User user, Date date, CodeComment comment) {
		super(user, date, comment.getProject());
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(comment);
	}
	
}
