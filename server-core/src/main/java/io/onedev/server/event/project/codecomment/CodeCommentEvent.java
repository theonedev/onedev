package io.onedev.server.event.project.codecomment;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.web.UrlService;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;

public abstract class CodeCommentEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	/**
	 * @param comment
	 * @param user
	 * @param date
	 */
	public CodeCommentEvent(User user, Date date, CodeComment comment) {
		super(user, date, comment.getProject());
		commentId = comment.getId();
	}

	public CodeComment getComment() {
		return OneDev.getInstance(CodeCommentService.class).load(commentId);
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlService.class).urlFor(getComment(), true);
	}
	
}
