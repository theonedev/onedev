package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.model.PullRequest;

@Singleton
public class PullRequestEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public PullRequestEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(PullRequestEvent event) {
		webSocketManager.notifyObservableChange(PullRequest.getWebSocketObservable(event.getRequest().getId()));
	}

}