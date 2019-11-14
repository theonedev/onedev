package io.onedev.server.search.entity.issue;

import java.util.Objects;

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
		Path<User> attribute = root.get(IssueConstants.ATTR_SUBMITTER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return Objects.equals(issue.getSubmitter(), user);
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
