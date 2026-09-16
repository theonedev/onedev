package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class DescriptionCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Expression<String> attribute = from.get(Issue.PROP_DESCRIPTION);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(Issue issue) {
		String description = issue.getDescription();
		return description != null && WildcardUtils.matchString("*" + value.toLowerCase() + "*", description.toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_DESCRIPTION) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Contains) + " " 
				+ quote(value);
	}

}
