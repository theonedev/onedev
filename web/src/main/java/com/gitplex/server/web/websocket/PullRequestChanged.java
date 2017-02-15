package com.gitplex.server.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.gitplex.server.web.util.AjaxEvent;

public class PullRequestChanged extends AjaxEvent {

	public PullRequestChanged(IPartialPageRequestHandler partialPageRequestHandler) {
		super(partialPageRequestHandler);
	}

}
