package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;

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
			return context.getBuilder().equal(join.get(IssueField.VALUE), value);
		else if (operator == IssueQueryLexer.IsNot)
			return context.getBuilder().notEqual(join.get(IssueField.VALUE), value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return context.getBuilder().greaterThan(join.get(IssueField.ORDINAL), ordinal);
		else
			return context.getBuilder().lessThan(join.get(IssueField.ORDINAL), ordinal);
	}

}
