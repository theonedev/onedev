package io.onedev.server.persistence.dao;

import io.onedev.server.model.AbstractEntity;

public class EntityPersisted {
	
	private final AbstractEntity entity;
	
	private final boolean isNew;
	
	public EntityPersisted(AbstractEntity entity, boolean isNew) {
		this.entity = entity;
		this.isNew = isNew;
	}

	public AbstractEntity getEntity() {
		return entity;
	}

	public boolean isNew() {
		return isNew;
	}
	
}
