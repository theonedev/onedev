package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;

public class ParamIsEmptyCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private String name;
	
	public ParamIsEmptyCriteria(String name) {
		this.name = name;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Join<?, ?> join1 = buildJoin.join(Build.PROP_PARAMS, JoinType.LEFT);
		join1.on(builder.and(
				builder.equal(join1.get(BuildParam.PROP_NAME), name)),
				builder.isNull(join1.get(BuildParam.PROP_VALUE)));
		Join<?, ?> join2 = buildJoin.join(Build.PROP_PARAMS, JoinType.LEFT);
		join2.on(builder.equal(join2.get(BuildParam.PROP_NAME), name));
		return builder.or(join1.isNotNull(), join2.isNull());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " + BuildMetricQuery.getRuleName(BuildMetricQueryLexer.IsEmpty);
	}
	
}
