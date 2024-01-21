package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;

public class PullRequestEmptyCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	public PullRequestEmptyCriteria(int operator) {
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		var predicate = builder.isNull(buildJoin.get(Build.PROP_PULL_REQUEST));
		if (operator == BuildMetricQueryLexer.IsNotEmpty)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_PULL_REQUEST) + " " + BuildMetricQuery.getRuleName(operator);
	}

}
