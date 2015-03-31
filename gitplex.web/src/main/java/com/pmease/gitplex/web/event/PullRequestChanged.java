package com.pmease.gitplex.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.model.PullRequest;

public class PullRequestChanged extends AjaxEvent {

	private final PullRequest request;
	
	public PullRequestChanged(AjaxRequestTarget target, PullRequest request) {
		super(target);
		
		this.request = request;
	}

	public PullRequest getRequest() {
		return request;
	}

}
