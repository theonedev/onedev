package com.gitplex.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.TaskChangeEvent;
import com.gitplex.server.web.websocket.WebSocketManager;

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