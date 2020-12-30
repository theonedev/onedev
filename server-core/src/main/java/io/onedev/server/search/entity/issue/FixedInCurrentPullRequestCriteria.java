package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

public class FixedInCurrentPullRequestCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		if (PullRequest.get() != null)
			return new FixedInPullRequestCriteria(PullRequest.get()).getPredicate(root, builder);
		else
			throw new ExplicitException("No pull request in query context");
	}

	@Override
	public boolean matches(Issue issue) {
		if (PullRequest.get() != null)
			return new FixedInPullRequestCriteria(PullRequest.get()).matches(issue);
		else
			throw new ExplicitException("No pull request in query context");
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInCurrentPullRequest);
	}

}
