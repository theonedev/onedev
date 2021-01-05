package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.support.BuildMetric;

public class ReportCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ReportCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<String> attribute = metricRoot.get(BuildMetric.PROP_REPORT);
		String normalized = value.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(BuildMetric.PROP_REPORT) + " " 
				+ BuildMetricQuery.getRuleName(BuildMetricQueryLexer.Is) + " " 
				+ quote(value);
	}

}
