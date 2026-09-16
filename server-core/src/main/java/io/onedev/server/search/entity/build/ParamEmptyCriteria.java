package io.onedev.server.search.entity.build;

import java.util.List;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ParamEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final int operator;
	
	public ParamEmptyCriteria(String name, int operator) {
		this.name = name;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Subquery<BuildParam> paramQuery = query.subquery(BuildParam.class);
		Root<BuildParam> paramRoot = paramQuery.from(BuildParam.class);
		paramQuery.select(paramRoot);

		var predicate = builder.not(builder.exists(paramQuery.where(
				builder.equal(paramRoot.get(BuildParam.PROP_BUILD), from), 
				builder.equal(paramRoot.get(BuildParam.PROP_NAME), name), 
				builder.isNotNull(paramRoot.get(BuildParam.PROP_VALUE)))));
		if (operator == BuildQueryLexer.IsNotEmpty)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		var matches = paramValues == null || paramValues.isEmpty();
		if (operator == BuildQueryLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " + BuildQuery.getRuleName(operator);
	}
	
}
