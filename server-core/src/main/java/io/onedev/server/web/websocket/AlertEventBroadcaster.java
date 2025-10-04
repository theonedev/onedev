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
	
	private final WebSocketService webSocketService;
	
	@Inject
	public AlertEventBroadcaster(WebSocketService webSocketService) {
		this.webSocketService = webSocketService;
	}

	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Alert) 
			webSocketService.notifyObservablesChange(Sets.newHashSet(Alert.getChangeObservable()), event.getSourcePage());
	}

	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Alert)
			webSocketService.notifyObservablesChange(Sets.newHashSet(Alert.getChangeObservable()), event.getSourcePage());
	}
	
}