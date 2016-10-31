package com.gitplex.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.gitplex.commons.wicket.AjaxEvent;

public class PullRequestChanged extends AjaxEvent {

	public PullRequestChanged(IPartialPageRequestHandler partialPageRequestHandler) {
		super(partialPageRequestHandler);
	}

}
