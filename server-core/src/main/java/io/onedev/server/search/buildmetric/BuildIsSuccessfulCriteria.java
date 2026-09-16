package io.onedev.server.search.buildmetric;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import io.onedev.server.model.Build;

public class BuildIsSuccessfulCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<?> attribute = buildJoin.get(Build.PROP_STATUS);
		return builder.equal(attribute, Build.Status.SUCCESSFUL);
	}

	@Override
	public String toStringWithoutParens() {
		return BuildMetricQuery.getRuleName(BuildMetricQueryLexer.BuildIsSuccessful);
	}

}
