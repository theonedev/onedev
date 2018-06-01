package io.onedev.server.model.support.issue.query;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;

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
		Path<String> attribute = context.getJoin(getFieldName()).get(IssueFieldUnary.VALUE);
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, String.valueOf(value));
		else 
			return context.getBuilder().notEqual(attribute, String.valueOf(value));
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return Objects.equals(value, getFieldValue(issue));
		else
			return !Objects.equals(value, getFieldValue(issue));
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getOperatorName(operator) + " " + IssueQuery.quote(String.valueOf(value));
	}

	@Override
	public void populate(Issue issue, Serializable fieldBean, Set<String> initedLists) {
		if (operator == IssueQueryLexer.Is)
			setFieldValue(fieldBean, value);
	}

}
