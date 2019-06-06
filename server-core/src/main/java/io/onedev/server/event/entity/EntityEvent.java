package io.onedev.server.event.entity;

import java.util.Date;

import io.onedev.server.event.Event;
import io.onedev.server.model.AbstractEntity;

public abstract class EntityEvent extends Event {
	
	private final AbstractEntity entity;
	
	public EntityEvent(AbstractEntity entity) {
		super(null, new Date());
		this.entity = entity;
	}

	public AbstractEntity getEntity() {
		return entity;
	}
	
}
