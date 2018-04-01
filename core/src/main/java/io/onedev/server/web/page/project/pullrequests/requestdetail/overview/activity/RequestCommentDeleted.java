package io.onedev.server.web.page.project.pullrequests.requestdetail.overview.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.util.AjaxPayload;

public class RequestCommentDeleted extends AjaxPayload {

	public RequestCommentDeleted(AjaxRequestTarget target) {
		super(target);
	}

}
