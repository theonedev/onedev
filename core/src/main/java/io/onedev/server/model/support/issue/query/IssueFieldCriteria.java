package io.onedev.server.model.support.issue.query;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;

public class IssueFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final long value;
	
	private final int operator;
	
	public IssueFieldCriteria(String name, long value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<Long> attribute = context.getJoin(getFieldName()).get(IssueFieldUnary.ORDINAL);
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else 
			return context.getBuilder().notEqual(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = getFieldValue(issue);
		if (operator == IssueQueryLexer.Is)
			return Objects.equals(fieldValue, value);
		else 
			return !Objects.equals(fieldValue, value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getOperatorName(operator) + " " + IssueQuery.quote(String.valueOf(value));
	}

}
