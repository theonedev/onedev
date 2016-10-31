package com.gitplex.web.page.depot.pullrequest.requestlist;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Order;

import com.gitplex.commons.util.WordUtils;

public class SortOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_ORDER_PROPERTY = "orderProperty";
	
	private static final String PARAM_ORDER_ASCENDING = "orderAscending";

	private final String propertyName;
	
	private final boolean ascending;
	
	public SortOption() {
		this("submitDate", false);
	}
	
	public SortOption(String propertyName, boolean ascending) {
		this.propertyName = propertyName;
		this.ascending = ascending;
	}

	public SortOption(PageParameters params) {
		propertyName = params.get(PARAM_ORDER_PROPERTY).toString("submitDate");
		ascending = params.get(PARAM_ORDER_ASCENDING).toBoolean(false); 
	}
	
	public Order getOrder() {
		if (ascending)
			return Order.asc(propertyName);
		else
			return Order.desc(propertyName);
	}
	
	public String getDisplayName() {
		String displayName = WordUtils.uncamel(propertyName).toLowerCase();
		if (ascending)
			return displayName + " ascending";
		else
			return displayName + " descending";
	}

	public void fillPageParams(PageParameters params) {
		params.set(PARAM_ORDER_PROPERTY, propertyName);
		params.set(PARAM_ORDER_ASCENDING, ascending);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SortOption))
			return false;
		if (this == other)
			return true;
		SortOption otherOption = (SortOption) other;
		return new EqualsBuilder()
				.append(propertyName, otherOption.propertyName)
				.append(ascending, otherOption.ascending)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(propertyName).append(ascending).toHashCode();
	}
		
}
