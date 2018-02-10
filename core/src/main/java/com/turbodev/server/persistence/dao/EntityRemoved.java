package com.turbodev.server.persistence.dao;

import com.turbodev.server.model.AbstractEntity;

public class EntityRemoved {
	
	private final AbstractEntity entity;
	
	public EntityRemoved(AbstractEntity entity) {
		this.entity = entity;
	}

	public AbstractEntity getEntity() {
		return entity;
	}
	
}
