package io.onedev.server.web.websocket;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.build.BuildEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BuildEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public BuildEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(BuildEvent event) {
		webSocketManager.notifyObservablesChange(event.getBuild().getChangeObservables(), event.getSourcePage());
	}

}