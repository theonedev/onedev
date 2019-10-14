package io.onedev.server.search.entity.issue;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.IssueConstants;

public class FixedInCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	public FixedInCriteria(Build build) {
		this.build = build;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Issue> root, CriteriaBuilder builder, User user) {
		Collection<Long> fixedIssueNumbers = build.getFixedIssueNumbers();
		if (!fixedIssueNumbers.isEmpty())
			return root.get(IssueConstants.ATTR_NUMBER).in(fixedIssueNumbers);
		else
			return builder.disjunction();
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return build.getFixedIssueNumbers().contains(issue.getNumber());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInBuild) + IssueQuery.quote("#" + build.getNumber());
	}

}
