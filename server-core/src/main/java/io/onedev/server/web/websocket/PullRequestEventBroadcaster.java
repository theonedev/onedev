package io.onedev.server.web.websocket;

import io.onedev.server.event.Listen;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.model.PullRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PullRequestEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public PullRequestEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(PullRequestEvent event) {
		webSocketManager.notifyObservableChange(PullRequest.getChangeObservable(event.getRequest().getId()), event.getSourcePage());
	}

}