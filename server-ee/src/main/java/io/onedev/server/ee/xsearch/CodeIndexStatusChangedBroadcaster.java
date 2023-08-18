package io.onedev.server.ee.xsearch;

import io.onedev.server.event.Listen;
import io.onedev.server.web.websocket.WebSocketManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CodeIndexStatusChangedBroadcaster {

	private final WebSocketManager webSocketManager;
	
	@Inject
	public CodeIndexStatusChangedBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}
	
	@Listen
	public void on(CodeIndexStatusChanged event) {
		webSocketManager.notifyObservableChange(CodeIndexStatusChanged.getChangeObservable(), null);
	}

}