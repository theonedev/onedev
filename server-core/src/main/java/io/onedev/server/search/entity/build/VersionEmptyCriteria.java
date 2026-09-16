package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class VersionEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	public VersionEmptyCriteria(int operator) {
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		var predicate = builder.isNull(from.get(Build.PROP_VERSION));
		if (operator == BuildQueryLexer.IsNotEmpty)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		var matches = build.getVersion() == null;
		if (operator == BuildQueryLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_VERSION) + " " + BuildQuery.getRuleName(operator);
	}

}
