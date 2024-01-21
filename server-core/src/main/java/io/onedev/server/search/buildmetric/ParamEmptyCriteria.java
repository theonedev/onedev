package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;

public class ParamEmptyCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final int operator;
	
	public ParamEmptyCriteria(String name, int operator) {
		this.name = name;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Join<?, ?> join1 = buildJoin.join(Build.PROP_PARAMS, JoinType.LEFT);
		join1.on(builder.and(
				builder.equal(join1.get(BuildParam.PROP_NAME), name)),
				builder.isNull(join1.get(BuildParam.PROP_VALUE)));
		Join<?, ?> join2 = buildJoin.join(Build.PROP_PARAMS, JoinType.LEFT);
		join2.on(builder.equal(join2.get(BuildParam.PROP_NAME), name));
		var predicate = builder.or(join1.isNotNull(), join2.isNull());
		if (operator == BuildMetricQueryLexer.IsNotEmpty)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " + BuildMetricQuery.getRuleName(operator);
	}
	
}
