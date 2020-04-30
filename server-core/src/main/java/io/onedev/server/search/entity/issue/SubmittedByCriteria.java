package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;

public class SubmittedByCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public SubmittedByCriteria(User user) {
		this.user = user;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Path<User> attribute = root.get(Issue.PROP_SUBMITTER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(Issue issue) {
		return Objects.equals(issue.getSubmitter(), user);
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.SubmittedBy) + " " + quote(user.getName());
	}

}
