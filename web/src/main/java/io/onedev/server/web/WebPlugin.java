package io.onedev.server.web;

import javax.inject.Inject;

import io.onedev.launcher.loader.AbstractPlugin;
import io.onedev.server.web.websocket.WebSocketManager;

public class WebPlugin extends AbstractPlugin {

	private final WebSocketManager webSocketManager;
	
	@Inject
	public WebPlugin(WebSocketManager webSocketManager) {
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