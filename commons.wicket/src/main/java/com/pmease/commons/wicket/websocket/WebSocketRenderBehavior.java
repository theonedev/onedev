package com.pmease.commons.wicket.websocket;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.cycle.RequestCycle;

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
		
		if (message.getText().equals(WebSocketManager.INITIAL_RENDER_CALLBACK) 
				|| message.getText().equals(WebSocketManager.RENDER_CALLBACK)) {
			onRender(handler);
		}
	}

	public Component getComponent() {
		return component;
	}
	
	@Override
	public void detach(Component component) {
		Boolean initialRender = RequestCycle.get().getMetaData(WebSocketManager.INITIAL_RENDER);
		if (initialRender != null && initialRender.booleanValue())
			onInitialRenderDetach();
		super.detach(component);
	}
	
	protected void onInitialRenderDetach() {
		
	}

	protected abstract void onRender(WebSocketRequestHandler handler);
	
}
