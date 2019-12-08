package io.onedev.server.search.entity.build;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.BuildQueryConstants;

public class ParamCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public ParamCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(BuildQueryConstants.ATTR_PARAMS, JoinType.LEFT);
		join.on(builder.and(
				builder.equal(join.get(BuildParam.ATTR_NAME), name)),
				builder.equal(join.get(BuildParam.ATTR_VALUE), value));
		return join.isNotNull();
	}

	@Override
	public boolean matches(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		if (paramValues == null || paramValues.isEmpty())
			return value == null;
		else 
			return paramValues.contains(value);
	}

	@Override
	public String toString() {
		return BuildQuery.quote(name) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) 
				+ " " + BuildQuery.quote(value);
	}
	
}
