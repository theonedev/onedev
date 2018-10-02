package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.pullrequest.PullRequestDeleted;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.util.WicketUtils;

@Singleton
public class PullRequestEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public PullRequestEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(PullRequestEvent event) {
		if (!(event instanceof PullRequestDeleted)) {
			PageKey sourcePageKey = WicketUtils.getPageKey();
			webSocketManager.notifyObservableChange(PullRequest.getWebSocketObservable(event.getRequest().getId()), sourcePageKey);
		}
	}

}