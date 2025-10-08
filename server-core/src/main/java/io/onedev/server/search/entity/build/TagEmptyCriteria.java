package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.Constants;

import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class TagEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	public TagEmptyCriteria(int operator) {
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Build.PROP_REF_NAME);
		var predicate = builder.or(
				builder.isNull(attribute), 
				builder.not(builder.like(attribute, Constants.R_TAGS + "%")));
		if (operator == BuildQueryLexer.IsNotEmpty)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		var matches = build.getTag() == null;
		if (operator == BuildQueryLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_TAG) + " " + BuildQuery.getRuleName(operator);
	}

}
