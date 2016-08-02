package com.pmease.commons.hibernate.dao;

import com.pmease.commons.hibernate.AbstractEntity;

public class EntityRemoved {
	
	private final AbstractEntity entity;
	
	public EntityRemoved(AbstractEntity entity) {
		this.entity = entity;
	}

	public AbstractEntity getEntity() {
		return entity;
	}
	
}
