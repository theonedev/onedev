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

public class ParamIsEmptyCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	public ParamIsEmptyCriteria(String name) {
		this.name = name;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Join<?, ?> join1 = root.join(Build.PROP_PARAMS, JoinType.LEFT);
		join1.on(builder.and(
				builder.equal(join1.get(BuildParam.PROP_NAME), name)),
				builder.isNull(join1.get(BuildParam.PROP_VALUE)));
		Join<?, ?> join2 = root.join(Build.PROP_PARAMS, JoinType.LEFT);
		join2.on(builder.equal(join2.get(BuildParam.PROP_NAME), name));
		return builder.or(join1.isNotNull(), join2.isNull());
	}

	@Override
	public boolean matches(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		return paramValues == null || paramValues.isEmpty();
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " + BuildQuery.getRuleName(BuildQueryLexer.IsEmpty);
	}
	
}
