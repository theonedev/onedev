package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.pmease.gitplex.core.entity.CodeComment;

public class CommentResized extends CommentEvent {

	public CommentResized(IPartialPageRequestHandler partialPageRequestHandlerndler, CodeComment comment) {
		super(partialPageRequestHandlerndler, comment);
	}

}
