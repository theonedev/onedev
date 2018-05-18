package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;

public class StateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public StateCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<?> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.STATE));
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else
			return context.getBuilder().notEqual(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getState().equals(value);
		else
			return !issue.getState().equals(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}
	
}
