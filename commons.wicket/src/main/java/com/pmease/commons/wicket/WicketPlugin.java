package com.pmease.commons.wicket;

import javax.inject.Inject;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.wicket.websocket.WebSocketManager;

public class WicketPlugin extends AbstractPlugin {

	private final WebSocketManager webSocketManager;
	
	@Inject
	public WicketPlugin(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}
	
	@Override
	public void start() {
		webSocketManager.start();
	}

	@Override
	public void stop() {
		webSocketManager.stop();
	}

}