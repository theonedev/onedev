package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;

public class NumberCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final long value;
	
	public NumberCriteria(long value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<Long> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.NUMBER));
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else if (operator == IssueQueryLexer.IsNot)
			return context.getBuilder().notEqual(attribute, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return context.getBuilder().greaterThan(attribute, value);
		else
			return context.getBuilder().lessThan(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getNumber() == value;
		else if (operator == IssueQueryLexer.IsNot)
			return issue.getNumber() != value;
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getNumber() > value;
		else
			return issue.getNumber() < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

}
