package com.pmease.gitplex.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.entity.PullRequest;

public class PullRequestChanged extends AjaxEvent {

	private final PullRequest request;
	
	public PullRequestChanged(IPartialPageRequestHandler partialPageRequestHandler, PullRequest request) {
		super(partialPageRequestHandler);
		
		this.request = request;
	}

	public PullRequest getRequest() {
		return request;
	}

}
