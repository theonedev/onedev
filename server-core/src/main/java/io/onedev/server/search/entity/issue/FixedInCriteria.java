package io.onedev.server.search.entity.issue;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.IssueQueryConstants;

public class FixedInCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	private final String value;
	
	public FixedInCriteria(@Nullable Project project, String value) {
		build = EntityQuery.getBuild(project, value);
		this.value = value;
	}

	public FixedInCriteria(Build build) {
		this.build = build;
		value = String.valueOf(build.getNumber());
	}
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Collection<Long> fixedIssueNumbers = build.getFixedIssueNumbers();
		if (!fixedIssueNumbers.isEmpty()) {
			return builder.and(
					builder.equal(root.get(IssueQueryConstants.ATTR_PROJECT), build.getProject()),
					root.get(IssueQueryConstants.ATTR_NUMBER).in(fixedIssueNumbers));
		} else {
			return builder.disjunction();
		}
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return issue.getProject().equals(build.getProject()) 
				&& build.getFixedIssueNumbers().contains(issue.getNumber());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInBuild) + " " + IssueQuery.quote(value);
	}

}
