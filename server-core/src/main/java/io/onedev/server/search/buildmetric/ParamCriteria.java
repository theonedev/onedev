package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;

public class ParamCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public ParamCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Join<?, ?> paramJoin = buildJoin.join(Build.PROP_PARAMS, JoinType.LEFT);
		paramJoin.on(builder.and(
				builder.equal(paramJoin.get(BuildParam.PROP_NAME), name)),
				builder.equal(paramJoin.get(BuildParam.PROP_VALUE), value));
		return paramJoin.isNotNull();
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " 
				+ BuildMetricQuery.getRuleName(BuildMetricQueryLexer.Is) + " " 
				+ quote(value);
	}
	
}
