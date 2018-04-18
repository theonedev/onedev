package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.util.WicketUtils;

@Singleton
public class PullRequestChangeBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public PullRequestChangeBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(PullRequestEvent event) {
		PageKey sourcePageKey = WicketUtils.getPageKey();
		webSocketManager.onObservableChanged(PullRequest.getWebSocketObservable(event.getRequest().getId()), sourcePageKey);
	}

}