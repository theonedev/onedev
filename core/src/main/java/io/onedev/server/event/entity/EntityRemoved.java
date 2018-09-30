package io.onedev.server.event.entity;

import io.onedev.server.model.AbstractEntity;

public class EntityRemoved extends EntityEvent {
	
	public EntityRemoved(AbstractEntity entity) {
		super(entity);
	}

}
