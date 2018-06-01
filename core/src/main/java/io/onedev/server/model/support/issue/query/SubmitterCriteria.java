package io.onedev.server.model.support.issue.query;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;

public class SubmitterCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final User value;
	
	private final int operator;
	
	public SubmitterCriteria(User value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<User> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.SUBMITTER));
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else
			return context.getBuilder().notEqual(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return Objects.equals(issue.getSubmitter(), value);
		else
			return !Objects.equals(issue.getSubmitter(), value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.SUBMITTER) + " " + IssueQuery.getOperatorName(operator) + " " + IssueQuery.quote(value.getName());
	}

}
