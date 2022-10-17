package io.onedev.server.event.entity;

import io.onedev.server.model.AbstractEntity;

public abstract class EntityEvent {
	
	private final AbstractEntity entity;
	
	public EntityEvent(AbstractEntity entity) {
		this.entity = entity;
	}

	public AbstractEntity getEntity() {
		return entity;
	}
	
}
