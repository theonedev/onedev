package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ConfidentialCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		return builder.equal(from.get(Issue.PROP_CONFIDENTIAL), true);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.isConfidential();
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.Confidential);
	}

}
