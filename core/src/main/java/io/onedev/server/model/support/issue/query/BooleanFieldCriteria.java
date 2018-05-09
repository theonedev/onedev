package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.IssueField;

public class BooleanFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final boolean value;
	
	private final int operator;
	
	public BooleanFieldCriteria(String name, boolean value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<String> attribute = context.getJoin(getFieldName()).get(IssueField.VALUE);
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, String.valueOf(value));
		else 
			return context.getBuilder().notEqual(attribute, String.valueOf(value));
	}

}
