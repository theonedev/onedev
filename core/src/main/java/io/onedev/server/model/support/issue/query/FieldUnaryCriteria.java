package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.IssueField;
import io.onedev.server.security.SecurityUtils;

public class FieldUnaryCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;

	public FieldUnaryCriteria(String name, int operator) {
		super(name);
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<?> attribute = context.getJoin(getFieldName()).get(IssueField.VALUE);
		if (operator == IssueQueryLexer.IsEmpty)
			return context.getBuilder().isNull(attribute);
		else if (operator == IssueQueryLexer.IsNotEmpty)
			return context.getBuilder().isNotNull(attribute);
		else if (operator == IssueQueryLexer.IsMe)
			return context.getBuilder().equal(attribute, SecurityUtils.getUser().getName());
		else 
			return context.getBuilder().notEqual(attribute, SecurityUtils.getUser());
	}

}
