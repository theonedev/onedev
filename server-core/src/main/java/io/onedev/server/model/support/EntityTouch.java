package io.onedev.server.model.support;

import io.onedev.server.model.AbstractEntity;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class EntityTouch extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public abstract Class<? extends AbstractEntity> getEntityClass();
	
	public abstract Long getProjectId();
	
	public abstract Long getEntityId();

}
