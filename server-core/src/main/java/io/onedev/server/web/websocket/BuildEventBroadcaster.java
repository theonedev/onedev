package io.onedev.server.web.websocket;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.build.BuildEvent;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class BuildEventBroadcaster {
	
	private final WebSocketService webSocketService;
	
	@Inject
	public BuildEventBroadcaster(WebSocketService webSocketService) {
		this.webSocketService = webSocketService;
	}

	@Listen
	public void on(BuildEvent event) {
		webSocketService.notifyObservablesChange(event.getBuild().getChangeObservables(), event.getSourcePage());
	}

}