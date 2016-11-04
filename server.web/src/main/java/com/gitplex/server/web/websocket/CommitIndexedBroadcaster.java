package com.gitplex.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.commons.loader.Listen;
import com.gitplex.commons.wicket.websocket.WebSocketManager;
import com.gitplex.server.search.CommitIndexed;

@Singleton
public class CommitIndexedBroadcaster {

	private final WebSocketManager webSocketManager;
	
	@Inject
	public CommitIndexedBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}
	
	@Listen
	public void on(CommitIndexed event) {
		CommitIndexedRegion region = new CommitIndexedRegion(event.getDepot().getId(), event.getCommitId());
		webSocketManager.render(region, null);
	}

}