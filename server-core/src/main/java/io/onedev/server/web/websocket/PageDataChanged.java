package io.onedev.server.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.web.util.AjaxPayload;

public class PageDataChanged extends AjaxPayload {

	public PageDataChanged(IPartialPageRequestHandler handler) {
		super(handler);
	}
	
}
