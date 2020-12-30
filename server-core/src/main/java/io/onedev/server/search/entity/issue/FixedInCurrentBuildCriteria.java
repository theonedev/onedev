package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;

public class FixedInCurrentBuildCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		if (Build.get() != null)
			return new FixedInBuildCriteria(Build.get()).getPredicate(root, builder);
		else
			throw new ExplicitException("No build in query context");
	}

	@Override
	public boolean matches(Issue issue) {
		if (Build.get() != null)
			return new FixedInBuildCriteria(Build.get()).matches(issue);
		else
			throw new ExplicitException("No build in query context");
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInCurrentBuild);
	}

}
