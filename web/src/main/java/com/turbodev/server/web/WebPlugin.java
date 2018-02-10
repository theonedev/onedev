package com.turbodev.server.web;

import javax.inject.Inject;

import com.turbodev.launcher.loader.AbstractPlugin;
import com.turbodev.server.web.websocket.WebSocketManager;

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