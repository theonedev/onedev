package com.gitplex.commons.wicket;

import javax.inject.Inject;

import com.gitplex.commons.loader.AbstractPlugin;
import com.gitplex.commons.wicket.websocket.WebSocketManager;

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