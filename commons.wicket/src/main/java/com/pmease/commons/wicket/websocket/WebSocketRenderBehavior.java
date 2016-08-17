package com.pmease.commons.wicket.websocket;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.cycle.RequestCycle;

@SuppressWarnings("serial")
public abstract class WebSocketRenderBehavior extends WebSocketBehavior {

	static final MetaDataKey<Boolean> ON_CONNECT = new MetaDataKey<Boolean>() {

		private static final long serialVersionUID = 1L;
		
	}; 
	
	private Component component;
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		this.component = component;
	}

	@Override
	protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
		super.onMessage(handler, message);
		
		if (message.getText().equals(WebSocketManager.RENDER_CALLBACK)) {
			onRender(handler);
		} else if (message.getText().equals(WebSocketManager.CONNECT_CALLBACK)) {
			/* 
			 * re-render interesting parts upon websocket connecting after a page is opened, 
			 * this is necessary in case some web socket render request is sent between the 
			 * gap of opening a page and a websocket connection is established. For instance
			 * when someone creates a pull request, the server will re-render integration 
			 * preview section of the page after preview is calculated and this may happen 
			 * before the web socket connection is established. Requiring the page to 
			 * re-render the integration preview section after connecting will make it 
			 * displaying correctly    
			 */
			RequestCycle.get().setMetaData(ON_CONNECT, true);
			onRender(handler);
		} 
 
	}

	public Component getComponent() {
		return component;
	}
	
	public boolean isOnConnect() {
		Boolean onConnect = RequestCycle.get().getMetaData(ON_CONNECT);
		return onConnect != null && onConnect.booleanValue();
	}
	
	protected abstract void onRender(WebSocketRequestHandler handler);
	
}
