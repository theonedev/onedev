package io.onedev.server.model.support.issue.query;

import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;

public class MultiChoiceFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public MultiChoiceFieldCriteria(String name, String value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<?> attribute = context.getJoin(getFieldName()).get(IssueFieldUnary.VALUE);
		if (operator == IssueQueryLexer.Contains)
			return context.getBuilder().equal(attribute, value);
		else
			return context.getBuilder().notEqual(attribute, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Issue issue) {
		List<String> fieldValue = (List<String>) getFieldValue(issue);
		if (operator == IssueQueryLexer.Contains)
			return fieldValue.contains(value);
		else
			return !fieldValue.contains(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}
	
}
