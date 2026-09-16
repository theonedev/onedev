package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class CurrentIssueCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (Issue.get() != null)
			return builder.equal(from, Issue.get());
		else
			throw new ExplicitException("No issue in query context");
	}

	@Override
	public boolean matches(Issue issue) {
		if (Issue.get() != null)
			return Issue.get().equals(issue);
		else
			throw new ExplicitException("No issue in query context");
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.CurrentIssue);
	}

}
