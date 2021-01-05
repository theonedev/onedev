package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;

public class BuildIsFailedCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<?> attribute = buildJoin.get(Build.PROP_STATUS);
		return builder.equal(attribute, Build.Status.FAILED);
	}

	@Override
	public String toStringWithoutParens() {
		return BuildMetricQuery.getRuleName(BuildMetricQueryLexer.BuildIsFailed);
	}

}
