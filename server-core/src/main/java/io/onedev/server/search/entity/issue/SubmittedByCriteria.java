package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.IssueQueryConstants;

public class SubmittedByCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	private final String value;
	
	public SubmittedByCriteria(String value) {
		user = EntityQuery.getUser(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Path<User> attribute = root.get(IssueQueryConstants.ATTR_SUBMITTER);
		return builder.equal(attribute, this.user);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return Objects.equals(issue.getSubmitter(), this.user);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}
	
	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.SubmittedBy) + " " + IssueQuery.quote(value);
	}

}
