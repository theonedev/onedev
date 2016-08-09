package com.pmease.gitplex.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.loader.Listen;
import com.pmease.commons.wicket.websocket.WebSocketManager;
import com.pmease.gitplex.search.CommitIndexed;

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
		webSocketManager.requestToRender(region, null, null);
	}

}