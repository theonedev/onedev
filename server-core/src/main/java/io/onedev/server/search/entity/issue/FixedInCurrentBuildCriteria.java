package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;

public class FixedInCurrentBuildCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Build build = Build.get();
		if (build != null)
			return new FixedInCriteria(build).getPredicate(root, builder, user);
		else
			return builder.disjunction();
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Build build = Build.get();
		if (build != null)
			return new FixedInCriteria(build).matches(issue, user);
		else
			return false;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInCurrentBuild);
	}

}
