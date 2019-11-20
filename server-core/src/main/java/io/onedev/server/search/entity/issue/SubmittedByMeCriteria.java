package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.util.IssueConstants;

public class SubmittedByMeCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		if (user != null) {
			Path<User> attribute = root.get(IssueConstants.ATTR_SUBMITTER);
			return builder.equal(attribute, user);
		} else {
			return builder.disjunction();
		}
	}

	@Override
	public boolean matches(Issue issue, User user) {
		if (user != null)
			return user.equals(issue.getSubmitter());
		else
			return false;
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.SubmittedByMe);
	}

}
