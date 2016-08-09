package com.pmease.commons.wicket.websocket;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.TextMessage;

@SuppressWarnings("serial")
public abstract class WebSocketRenderBehavior extends WebSocketBehavior {

	private Component component;
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		this.component = component;
	}

	@Override
	protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
		super.onMessage(handler, message);
		
		if (message.getText().equals("RenderCallback")) {
			onRender(handler);
		}
	}

	public Component getComponent() {
		return component;
	}
	
	protected abstract void onRender(WebSocketRequestHandler handler);
	
}
