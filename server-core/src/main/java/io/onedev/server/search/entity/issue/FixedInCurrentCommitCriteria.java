package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneException;
import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScopedCommit;

public class FixedInCurrentCommitCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		if (ProjectScopedCommit.get() != null)
			return new FixedInCommitCriteria(ProjectScopedCommit.get()).getPredicate(root, builder);
		else
			throw new OneException("No commit id in query context");
	}

	@Override
	public boolean matches(Issue issue) {
		if (ProjectScopedCommit.get() != null)
			return new FixedInCommitCriteria(ProjectScopedCommit.get()).matches(issue);
		else
			throw new OneException("No commit in query context");
	}

	@Override
	public String asString() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInCurrentCommit);
	}

}
