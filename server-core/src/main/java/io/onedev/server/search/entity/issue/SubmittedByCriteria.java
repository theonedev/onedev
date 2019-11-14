package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.util.IssueConstants;

public class SubmittedByCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final User value;
	
	private String rawValue;
	
	public SubmittedByCriteria(User value, String rawValue) {
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Path<User> attribute = root.get(IssueConstants.ATTR_SUBMITTER);
		return builder.equal(attribute, value);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return Objects.equals(issue.getSubmitter(), value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}
	
	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.SubmittedBy) + " " + IssueQuery.quote(rawValue);
	}

}
