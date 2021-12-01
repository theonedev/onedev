package io.onedev.server.search.entity.issue;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

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
		value = build.getFQN().toString();
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Collection<Long> fixedIssueNumbers = build.getFixedIssueNumbers();
		if (!fixedIssueNumbers.isEmpty()) {
			return builder.and(
					builder.equal(from.get(Issue.PROP_PROJECT), build.getProject()),
					from.get(Issue.PROP_NUMBER).in(fixedIssueNumbers));
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
