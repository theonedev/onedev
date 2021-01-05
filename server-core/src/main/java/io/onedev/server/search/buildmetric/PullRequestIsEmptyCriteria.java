package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;

public class PullRequestIsEmptyCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		return builder.isNull(buildJoin.get(Build.PROP_PULL_REQUEST));
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_PULL_REQUEST) + " " + BuildMetricQuery.getRuleName(BuildMetricQueryLexer.IsEmpty);
	}

}
