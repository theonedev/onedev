package io.onedev.server.event.entity;

import io.onedev.server.model.AbstractEntity;

public class EntityPersisted extends EntityEvent {
	
	private final boolean isNew;
	
	public EntityPersisted(AbstractEntity entity, boolean isNew) {
		super(entity);
		this.isNew = isNew;
	}

	public boolean isNew() {
		return isNew;
	}
	
}
