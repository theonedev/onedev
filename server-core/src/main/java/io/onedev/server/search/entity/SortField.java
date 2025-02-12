package io.onedev.server.search.entity;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.search.entity.EntitySort.Direction;

import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;

public class SortField<T extends AbstractEntity> {

	private final String property;

	private final Direction defaultDirection;

	public SortField(String property, Direction defaultDirection) {
		this.property = property;
		this.defaultDirection = defaultDirection;
	}

	public SortField(String property) {
		this(property, ASCENDING);
	}

	public String getProperty() {
		return property;
	}

	public Direction getDefaultDirection() {
		return defaultDirection;
	}
}
