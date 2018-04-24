package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;

public class DescriptionCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public DescriptionCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Expression<String> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.DESCRIPTION));
		if (operator == IssueQueryLexer.Contains)
			return context.getBuilder().like(attribute, "%" + value + "%");
		else
			return context.getBuilder().notLike(attribute, "%" + value + "%");
	}

}
