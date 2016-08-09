package com.pmease.gitplex.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.pmease.commons.wicket.AjaxEvent;

public class PullRequestChanged extends AjaxEvent {

	public PullRequestChanged(IPartialPageRequestHandler partialPageRequestHandler) {
		super(partialPageRequestHandler);
	}

}
