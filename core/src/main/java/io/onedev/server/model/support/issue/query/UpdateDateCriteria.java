package io.onedev.server.model.support.issue.query;

import java.util.Date;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;

public class UpdateDateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public UpdateDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<Long> attribute = getPath(context.getRoot(), Issue.BUILTIN_FIELDS.get(Issue.UPDATE_DATE));
		if (operator == IssueQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value.getTime());
		else
			return context.getBuilder().greaterThan(attribute, value.getTime());
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.IsBefore)
			return issue.getLastActivity().getDate().before(value);
		else
			return issue.getLastActivity().getDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return quote(Issue.UPDATE_DATE) + " " + IssueQuery.getOperatorName(operator) + " " + quote(rawValue);
	}

}
