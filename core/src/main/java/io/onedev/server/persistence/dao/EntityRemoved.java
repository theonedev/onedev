package io.onedev.server.persistence.dao;

import io.onedev.server.model.AbstractEntity;

public class EntityRemoved {
	
	private final AbstractEntity entity;
	
	public EntityRemoved(AbstractEntity entity) {
		this.entity = entity;
	}

	public AbstractEntity getEntity() {
		return entity;
	}
	
}
