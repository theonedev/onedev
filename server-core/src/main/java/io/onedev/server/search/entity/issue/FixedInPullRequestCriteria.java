package io.onedev.server.search.entity.issue;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;

public class FixedInPullRequestCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final PullRequest request;
	
	private final String value;
	
	public FixedInPullRequestCriteria(@Nullable Project project, String value) {
		request = EntityQuery.getPullRequest(project, value);
		this.value = value;
	}

	public FixedInPullRequestCriteria(PullRequest request) {
		this.request = request;
		value = String.valueOf(request.getNumber());
	}
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Collection<Long> fixedIssueNumbers = request.getFixedIssueNumbers();
		if (!fixedIssueNumbers.isEmpty()) {
			return builder.and(
					builder.equal(root.get(Issue.PROP_PROJECT), request.getTargetProject()),
					root.get(Issue.PROP_NUMBER).in(fixedIssueNumbers));
		} else {
			return builder.disjunction();
		}
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getProject().equals(request.getTargetProject()) 
				&& request.getFixedIssueNumbers().contains(issue.getNumber());
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInPullRequest) + " " + quote(value);
	}

}
