package io.onedev.server.web.editable;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.web.util.AjaxPayload;

public class BeanUpdating extends AjaxPayload {

	public BeanUpdating(IPartialPageRequestHandler target) {
		super(target);
	}

}