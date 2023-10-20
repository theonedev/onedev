package io.onedev.server.event.entity;

import io.onedev.server.model.AbstractEntity;

public class EntityPersisted extends EntityEvent {
	
	private final boolean newEntity;
	
	public EntityPersisted(AbstractEntity entity, boolean newEntity) {
		super(entity);
		this.newEntity = newEntity;
	}

	public boolean isNewEntity() {
		return newEntity;
	}
	
}
