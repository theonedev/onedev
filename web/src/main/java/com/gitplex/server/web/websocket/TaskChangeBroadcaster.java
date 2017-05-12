package com.gitplex.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.model.PullRequestTask;
import com.gitplex.server.persistence.dao.EntityPersisted;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.web.websocket.WebSocketManager;

@Singleton
public class TaskChangeBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public TaskChangeBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof PullRequestTask) {
			PullRequestTask task = (PullRequestTask) event.getEntity();
			TaskChangedRegion region = new TaskChangedRegion(task.getUser().getId());
			webSocketManager.render(region, null);
		}
	}

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof PullRequestTask) {
			PullRequestTask task = (PullRequestTask) event.getEntity();
			TaskChangedRegion region = new TaskChangedRegion(task.getUser().getId());
			webSocketManager.render(region, null);
		}
	}
	
}