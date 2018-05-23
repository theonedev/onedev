package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.model.Task;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityPersisted;
import io.onedev.server.persistence.dao.EntityRemoved;

@Singleton
public class TaskEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public TaskEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Task) {
			Task task = (Task) event.getEntity();
			webSocketManager.onObservableChanged(User.getWebSocketObservable(task.getUser().getId()), null);
		}
	}

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Task) {
			Task task = (Task) event.getEntity();
			webSocketManager.onObservableChanged(User.getWebSocketObservable(task.getUser().getId()), null);
		}
	}
	
}