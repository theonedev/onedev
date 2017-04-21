package com.gitplex.server.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.gitplex.server.web.util.AjaxPayload;

public class PullRequestChanged extends AjaxPayload {

	public PullRequestChanged(IPartialPageRequestHandler partialPageRequestHandler) {
		super(partialPageRequestHandler);
	}

}
