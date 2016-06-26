package com.pmease.gitplex.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.entity.CodeComment;

public class CodeCommentChanged extends AjaxEvent {

	private final CodeComment comment;
	
	public CodeCommentChanged(IPartialPageRequestHandler partialPageRequestHandler, CodeComment comment) {
		super(partialPageRequestHandler);
		
		this.comment = comment;
	}

	public CodeComment getComment() {
		return comment;
	}

}
