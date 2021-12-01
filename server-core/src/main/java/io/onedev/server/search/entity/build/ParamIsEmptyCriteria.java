package io.onedev.server.search.entity.build;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

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
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Subquery<BuildParam> paramQuery = query.subquery(BuildParam.class);
		Root<BuildParam> paramRoot = paramQuery.from(BuildParam.class);
		paramQuery.select(paramRoot);

		return builder.not(builder.exists(paramQuery.where(
				builder.equal(paramRoot.get(BuildParam.PROP_BUILD), from), 
				builder.equal(paramRoot.get(BuildParam.PROP_NAME), name), 
				builder.isNotNull(paramRoot.get(BuildParam.PROP_VALUE)))));
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
