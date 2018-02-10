package com.turbodev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.launcher.loader.Listen;
import com.turbodev.server.search.CommitIndexed;
import com.turbodev.server.web.websocket.WebSocketManager;

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