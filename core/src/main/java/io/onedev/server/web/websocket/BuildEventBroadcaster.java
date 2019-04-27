package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.event.build.BuildDeleted;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.model.Build;
import io.onedev.server.web.util.WicketUtils;

@Singleton
public class BuildEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public BuildEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(BuildEvent event) {
		if (!(event instanceof BuildDeleted))
			webSocketManager.notifyObservableChange(Build.getWebSocketObservable(event.getBuild().getId()), WicketUtils.getPageKey());
	}

}