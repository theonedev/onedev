package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;

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
