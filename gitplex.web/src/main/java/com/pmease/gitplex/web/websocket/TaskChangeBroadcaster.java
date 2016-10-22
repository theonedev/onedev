package com.pmease.gitplex.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.loader.Listen;
import com.pmease.commons.wicket.websocket.WebSocketManager;
import com.pmease.gitplex.core.event.TaskChangeEvent;

@Singleton
public class TaskChangeBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public TaskChangeBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(TaskChangeEvent event) {
		TaskChangedRegion region = new TaskChangedRegion(event.getUserId());
		webSocketManager.render(region, null);
	}

}