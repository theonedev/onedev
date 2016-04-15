package com.pmease.commons.wicket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

public class AjaxEvent {
	
	private final IPartialPageRequestHandler partialPageRequestHandler;
	
	public AjaxEvent(IPartialPageRequestHandler partialPageRequestHandler) {
		this.partialPageRequestHandler = partialPageRequestHandler;
	}

	public IPartialPageRequestHandler getPartialPageRequestHandler() {
		return partialPageRequestHandler;
	}
	
}
