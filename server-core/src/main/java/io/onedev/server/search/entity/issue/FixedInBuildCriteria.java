package io.onedev.server.search.entity.issue;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

import io.onedev.server.search.entity.EntityQuery;

public class FixedInBuildCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final Build build;
	
	private final String value;
	
	public FixedInBuildCriteria(@Nullable Project project, String value) {
		build = EntityQuery.getBuild(project, value);
		this.value = value;
	}

	public FixedInBuildCriteria(Build build) {
		this.build = build;
		value = String.valueOf(build.getNumber());
	}
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Collection<Long> fixedIssueNumbers = build.getFixedIssueNumbers();
		if (!fixedIssueNumbers.isEmpty()) {
			return builder.and(
					builder.equal(root.get(Issue.PROP_PROJECT), build.getProject()),
					root.get(Issue.PROP_NUMBER).in(fixedIssueNumbers));
		} else {
			return builder.disjunction();
		}
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getProject().equals(build.getProject()) 
				&& build.getFixedIssueNumbers().contains(issue.getNumber());
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInBuild) + " " + quote(value);
	}

}
