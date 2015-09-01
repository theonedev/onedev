package com.pmease.gitplex.web.websocket;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.model.PullRequest;

public class PullRequestChanged extends AjaxEvent {

	private final PullRequest request;
	
	private final PullRequest.Event event;
	
	public PullRequestChanged(AjaxRequestTarget target, PullRequest request, PullRequest.Event event) {
		super(target);
		
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
