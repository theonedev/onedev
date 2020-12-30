package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;

public class SubmittedByMeCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		if (User.get() != null) {
			Path<User> attribute = root.get(Issue.PROP_SUBMITTER);
			return builder.equal(attribute, User.get());
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(Issue issue) {
		if (User.get() != null)
			return User.get().equals(issue.getSubmitter());
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.SubmittedByMe);
	}

}
