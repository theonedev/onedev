package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.model.PullRequestTask;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityPersisted;
import io.onedev.server.persistence.dao.EntityRemoved;

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
			webSocketManager.onObservableChanged(User.getWebSocketObservable(task.getUser().getId()), null);
		}
	}

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof PullRequestTask) {
			PullRequestTask task = (PullRequestTask) event.getEntity();
			webSocketManager.onObservableChanged(User.getWebSocketObservable(task.getUser().getId()), null);
		}
	}
	
}