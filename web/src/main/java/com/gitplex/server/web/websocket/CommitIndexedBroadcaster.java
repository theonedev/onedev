package com.gitplex.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.search.CommitIndexed;
import com.gitplex.server.web.websocket.WebSocketManager;

@Singleton
public class CommitIndexedBroadcaster {

	private final WebSocketManager webSocketManager;
	
	@Inject
	public CommitIndexedBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}
	
	@Listen
	public void on(CommitIndexed event) {
		CommitIndexedRegion region = new CommitIndexedRegion(event.getCommitId());
		webSocketManager.render(region, null);
	}

}