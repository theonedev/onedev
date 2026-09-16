package io.onedev.server.search.buildmetric;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class NotBuildMetricCriteria extends BuildMetricCriteria {
	
	private static final long serialVersionUID = 1L;

	private final BuildMetricCriteria criteria;
	
	public NotBuildMetricCriteria(BuildMetricCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		return criteria.getPredicate(metricRoot, buildJoin, builder).not();
	}

	@Override
	public String toStringWithoutParens() {
		return new NotBuildMetricCriteria(criteria).toStringWithoutParens();
	}
	
}
