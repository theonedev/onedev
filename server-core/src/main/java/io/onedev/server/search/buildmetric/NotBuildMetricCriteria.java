package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
