package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.criteria.Criteria;

public class FixedInCurrentCommitCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (ProjectScopedCommit.get() != null)
			return new FixedInCommitCriteria(ProjectScopedCommit.get()).getPredicate(projectScope, query, from, builder);
		else
			throw new ExplicitException("No commit id in query context");
	}

	@Override
	public boolean matches(Issue issue) {
		if (ProjectScopedCommit.get() != null)
			return new FixedInCommitCriteria(ProjectScopedCommit.get()).matches(issue);
		else
			throw new ExplicitException("No commit in query context");
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInCurrentCommit);
	}

}
