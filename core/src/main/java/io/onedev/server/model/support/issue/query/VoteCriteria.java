package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;

public class VoteCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public VoteCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		String fieldName = Issue.BUILTIN_FIELDS.get(Issue.VOTES);
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(context.getRoot().get(fieldName), value);
		else if (operator == IssueQueryLexer.IsNot)
			return context.getBuilder().notEqual(context.getRoot().get(fieldName), value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return context.getBuilder().greaterThan(context.getRoot().get(fieldName), value);
		else
			return context.getBuilder().lessThan(context.getRoot().get(fieldName), value);
	}

}
