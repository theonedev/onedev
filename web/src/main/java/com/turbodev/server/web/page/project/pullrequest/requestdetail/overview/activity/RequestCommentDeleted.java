package com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.turbodev.server.web.util.AjaxPayload;

public class RequestCommentDeleted extends AjaxPayload {

	public RequestCommentDeleted(AjaxRequestTarget target) {
		super(target);
	}

}
