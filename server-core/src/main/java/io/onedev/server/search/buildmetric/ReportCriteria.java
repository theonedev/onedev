package io.onedev.server.search.buildmetric;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import io.onedev.server.model.support.BuildMetric;

public class ReportCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public ReportCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<String> attribute = metricRoot.get(BuildMetric.PROP_REPORT);
		String normalized = value.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == BuildMetricQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(BuildMetric.PROP_REPORT) + " " 
				+ BuildMetricQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
