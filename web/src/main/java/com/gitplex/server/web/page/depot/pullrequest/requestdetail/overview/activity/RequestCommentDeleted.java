package com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.web.util.AjaxEvent;

public class RequestCommentDeleted extends AjaxEvent {

	public RequestCommentDeleted(AjaxRequestTarget target) {
		super(target);
	}

}
