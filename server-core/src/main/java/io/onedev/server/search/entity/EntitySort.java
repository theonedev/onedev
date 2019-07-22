package io.onedev.server.search.entity;

import java.io.Serializable;

public class EntitySort implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public enum Direction {ASCENDING, DESCENDING};
	
	private String field;
	
	private EntitySort.Direction direction = Direction.ASCENDING;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public EntitySort.Direction getDirection() {
		return direction;
	}

	public void setDirection(EntitySort.Direction direction) {
		this.direction = direction;
	}

	@Override
	public String toString() {
		if (direction == Direction.ASCENDING)
			return EntityQuery.quote(field) + " asc";
		else
			return EntityQuery.quote(field) + " desc";
	}
	
}