package io.onedev.server.model.support.issue.query;

import java.util.Objects;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;

public class ChoiceFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final long ordinal;
	
	private final int operator;
	
	public ChoiceFieldCriteria(String name, String value, long ordinal, int operator) {
		super(name);
		this.value = value;
		this.ordinal = ordinal;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Join<Issue, ?> join = context.getJoin(getFieldName());
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(join.get(IssueFieldUnary.VALUE), value);
		else if (operator == IssueQueryLexer.IsNot)
			return context.getBuilder().notEqual(join.get(IssueFieldUnary.VALUE), value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return context.getBuilder().greaterThan(join.get(IssueFieldUnary.ORDINAL), ordinal);
		else
			return context.getBuilder().lessThan(join.get(IssueFieldUnary.ORDINAL), ordinal);
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = getFieldValue(issue);
		if (operator == IssueQueryLexer.Is)
			return Objects.equals(fieldValue, value);
		else if (operator == IssueQueryLexer.IsNot)
			return !Objects.equals(fieldValue, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return getFieldOrdinal(issue) > ordinal;
		else
			return getFieldOrdinal(issue) < ordinal;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

}
