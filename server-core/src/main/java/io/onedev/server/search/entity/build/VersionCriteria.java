package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class VersionCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public VersionCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Build.PROP_VERSION);
		String normalized = value.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == BuildQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		String version = build.getVersion();
		var matches = version != null && WildcardUtils.matchString(value.toLowerCase(), version.toLowerCase());
		if (operator == BuildQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_VERSION) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
