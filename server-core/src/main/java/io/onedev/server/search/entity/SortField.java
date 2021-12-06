package io.onedev.server.search.entity;

import java.util.Comparator;

import io.onedev.server.model.AbstractEntity;

public class SortField<T extends AbstractEntity> {

	private final String property;
	
	private final Comparator<T> comparator;
	
	public SortField(String property, Comparator<T> comparator) {
		this.property = property;
		this.comparator = comparator;
	}

	public String getProperty() {
		return property;
	}

	public Comparator<T> getComparator() {
		return comparator;
	}
	
}
