package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.model.PullRequestTask;
import io.onedev.server.persistence.dao.EntityPersisted;
import io.onedev.server.persistence.dao.EntityRemoved;
import io.onedev.server.web.websocket.WebSocketManager;

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