package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.entity.Comment;

public class CommentEvent extends AjaxEvent {

	private final Comment comment;
	
	public CommentEvent(IPartialPageRequestHandler partialPageRequestHandlerndler, Comment comment) {
		super(partialPageRequestHandlerndler);
		
		this.comment = comment;
	}

	public Comment getComment() {
		return comment;
	}

}
