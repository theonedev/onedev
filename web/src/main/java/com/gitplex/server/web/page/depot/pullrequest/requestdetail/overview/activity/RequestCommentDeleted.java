package com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.web.util.AjaxPayload;

public class RequestCommentDeleted extends AjaxPayload {

	public RequestCommentDeleted(AjaxRequestTarget target) {
		super(target);
	}

}
