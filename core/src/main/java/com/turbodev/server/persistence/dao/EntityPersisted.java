package com.turbodev.server.persistence.dao;

import com.turbodev.server.model.AbstractEntity;

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
