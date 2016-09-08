package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;

public class RequestCommentDeleted extends AjaxEvent {

	public RequestCommentDeleted(AjaxRequestTarget target) {
		super(target);
	}

}
