package io.onedev.server.search.buildmetric;

import java.util.Date;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.util.QueryUtils;

public class DateCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date date;
	
	private final String value;
	
	public DateCriteria(String value, int operator) {
		date = QueryUtils.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<Date> attribute = buildJoin.get(Build.PROP_FINISH_DATE);
		if (operator == BuildMetricQueryLexer.Until)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public String toStringWithoutParens() {
		return BuildMetricQuery.getRuleName(operator) + " " + quote(value);
	}

}
