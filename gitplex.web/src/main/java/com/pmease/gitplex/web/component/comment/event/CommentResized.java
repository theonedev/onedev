package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.pmease.gitplex.core.entity.Comment;

public class CommentResized extends CommentEvent {

	public CommentResized(IPartialPageRequestHandler partialPageRequestHandlerndler, Comment comment) {
		super(partialPageRequestHandlerndler, comment);
	}

}
