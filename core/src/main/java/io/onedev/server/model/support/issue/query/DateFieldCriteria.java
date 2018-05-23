package io.onedev.server.model.support.issue.query;

import java.util.Date;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;

public class DateFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Date value;
	
	private final int operator;
	
	public DateFieldCriteria(String name, Date value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Join<Issue, ?> join = context.getJoin(getFieldName());
		if (operator == IssueQueryLexer.IsBefore)
			return context.getBuilder().lessThan(join.get(IssueFieldUnary.ORDINAL), value.getTime());
		else
			return context.getBuilder().greaterThan(join.get(IssueFieldUnary.ORDINAL), value.getTime());
	}

	@Override
	public boolean matches(Issue issue) {
		Date fieldValue = (Date) getFieldValue(issue);
		if (operator == IssueQueryLexer.IsBefore)
			return fieldValue != null && fieldValue.before(value);
		else
			return fieldValue != null && fieldValue.after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

}
