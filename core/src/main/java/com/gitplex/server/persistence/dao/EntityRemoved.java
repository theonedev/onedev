package com.gitplex.server.persistence.dao;

import com.gitplex.server.model.AbstractEntity;

public class EntityRemoved {
	
	private final AbstractEntity entity;
	
	public EntityRemoved(AbstractEntity entity) {
		this.entity = entity;
	}

	public AbstractEntity getEntity() {
		return entity;
	}
	
}
