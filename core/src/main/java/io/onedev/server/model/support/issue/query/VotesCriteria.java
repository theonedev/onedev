package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;

public class VotesCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public VotesCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<Integer> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.VOTES));
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
			return issue.getNumOfVotes() == value;
		else if (operator == IssueQueryLexer.IsNot)
			return issue.getNumOfVotes() != value;
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getNumOfVotes() > value;
		else
			return issue.getNumOfVotes() < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return quote(Issue.VOTES) + " " + IssueQuery.getOperatorName(operator) + " " + quote(String.valueOf(value));
	}

}
