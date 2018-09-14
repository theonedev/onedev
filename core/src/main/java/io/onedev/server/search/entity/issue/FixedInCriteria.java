package io.onedev.server.search.entity.issue;

import java.util.Collection;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueConstants;
import io.onedev.server.search.entity.QueryBuildContext;

public class FixedInCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	public FixedInCriteria(Build build) {
		this.build = build;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		Collection<Long> fixedIssueNumbers = build.getFixedIssueNumbers(null);
		if (!fixedIssueNumbers.isEmpty())
			return context.getRoot().get(IssueConstants.ATTR_NUMBER).in(fixedIssueNumbers);
		else
			return context.getBuilder().disjunction();
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return build.getFixedIssueNumbers(null).contains(issue.getNumber());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInBuild) + " " + IssueQuery.quote(build.getName());
	}

}
