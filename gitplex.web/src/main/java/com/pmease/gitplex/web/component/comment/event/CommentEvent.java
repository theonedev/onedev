package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.entity.CodeComment;

public class CommentEvent extends AjaxEvent {

	private final CodeComment comment;
	
	public CommentEvent(IPartialPageRequestHandler partialPageRequestHandlerndler, CodeComment comment) {
		super(partialPageRequestHandlerndler);
		
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

}
