package com.pmease.gitplex.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.entity.PullRequest;

public class PullRequestChanged extends AjaxEvent {

	private final PullRequest request;
	
	private final PullRequest.Event event;
	
	public PullRequestChanged(IPartialPageRequestHandler partialPageRequestHandler, 
			PullRequest request, PullRequest.Event event) {
		super(partialPageRequestHandler);
		
		this.request = request;
		this.event = event;
	}

	public PullRequest getRequest() {
		return request;
	}

	public PullRequest.Event getEvent() {
		return event;
	}

}
