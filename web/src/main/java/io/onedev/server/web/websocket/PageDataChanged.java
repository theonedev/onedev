package io.onedev.server.web.websocket;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.web.util.AjaxPayload;

public class PageDataChanged extends AjaxPayload {

	private final boolean onConnect;
	
	public PageDataChanged(IPartialPageRequestHandler handler, boolean onConnect) {
		super(handler);
		this.onConnect = onConnect;
	}

	public PageDataChanged(IPartialPageRequestHandler handler) {
		this(handler, false);
	}
	
	public boolean isOnConnect() {
		return onConnect;
	}
	
}
