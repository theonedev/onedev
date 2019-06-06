package io.onedev.server.web.util;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

public class AjaxPayload {
	
	private final IPartialPageRequestHandler handler;
	
	public AjaxPayload(IPartialPageRequestHandler handler) {
		this.handler = handler;
	}

	public IPartialPageRequestHandler getHandler() {
		return handler;
	}
	
}
