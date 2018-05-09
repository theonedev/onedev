package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.IssueField;

public class IssueFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final int value;
	
	private final int operator;
	
	public IssueFieldCriteria(String name, int value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<Integer> attribute = context.getJoin(getFieldName()).get(IssueField.ORDINAL);
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else 
			return context.getBuilder().notEqual(attribute, value);
	}

}
