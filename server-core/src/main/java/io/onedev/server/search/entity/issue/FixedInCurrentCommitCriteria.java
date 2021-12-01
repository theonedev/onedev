package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScopedCommit;

public class FixedInCurrentCommitCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (ProjectScopedCommit.get() != null)
			return new FixedInCommitCriteria(ProjectScopedCommit.get()).getPredicate(query, from, builder);
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
