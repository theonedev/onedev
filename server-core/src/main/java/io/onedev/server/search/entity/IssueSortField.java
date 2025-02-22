package io.onedev.server.search.entity;

import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;

import java.util.Comparator;

import io.onedev.server.model.Issue;
import io.onedev.server.search.entity.EntitySort.Direction;

public class IssueSortField extends SortField<Issue> {

	private final Comparator<Issue> comparator;

	public IssueSortField(String property, Direction defaultDirection, Comparator<Issue> comparator) {
		super(property, defaultDirection);
		this.comparator = comparator;
	}

	public IssueSortField(String property, Comparator<Issue> comparator) {
		this(property, ASCENDING, comparator);
	}

	public Comparator<Issue> getComparator() {
		return comparator;
	}

}
