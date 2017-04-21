package com.gitplex.server.web.util;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

public class AjaxPayload {
	
	private final IPartialPageRequestHandler partialPageRequestHandler;
	
	public AjaxPayload(IPartialPageRequestHandler partialPageRequestHandler) {
		this.partialPageRequestHandler = partialPageRequestHandler;
	}

	public IPartialPageRequestHandler getPartialPageRequestHandler() {
		return partialPageRequestHandler;
	}
	
}
