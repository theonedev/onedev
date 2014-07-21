package com.pmease.gitplex.web.page.repository.pullrequest;

import org.hibernate.criterion.Order;

public enum SortOption {
	
	CREATE_DATE_DESCENDING("createDate", false, "newest create date"),
	
	CREATE_DATE_ASCENDING("createDate", true, "oldest create date"),
	
	UPDATE_DATE_DESCENDING("updateDate", false, "newest update date"),

	UPDATE_DATE_ASCENDING("updateDate", true, "oldest update date");

	private final String displayName;
	
	private final String propertyName;
	
	private final boolean ascending;
	
	SortOption(String propertyName, boolean ascending, String displayName) {
		this.propertyName = propertyName;
		this.ascending = ascending;
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
	public Order getOrder() {
		if (ascending)
			return Order.asc(propertyName);
		else
			return Order.desc(propertyName);
	}
}
