package io.onedev.server.web.websocket;

import com.google.common.collect.Sets;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.Alert;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AlertEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public AlertEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Alert) 
			webSocketManager.notifyObservablesChange(Sets.newHashSet(Alert.getChangeObservable()), event.getSourcePage());
	}

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Alert)
			webSocketManager.notifyObservablesChange(Sets.newHashSet(Alert.getChangeObservable()), event.getSourcePage());
	}
	
}