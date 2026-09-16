package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.Constants;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class TagCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public TagCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Build.PROP_REF_NAME);
		String normalized = Constants.R_TAGS + value.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == BuildQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		String tag = build.getTag();
		var matches = tag != null && WildcardUtils.matchString(value.toLowerCase(), tag.toLowerCase());
		if (operator == BuildQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_TAG) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
