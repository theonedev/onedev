package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;

public class TitleCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public TitleCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<String> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.TITLE));
		if (operator == IssueQueryLexer.Contains)
			return context.getBuilder().like(attribute, "%" + value + "%");
		else
			return context.getBuilder().notLike(attribute, "%" + value + "%");
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Contains)
			return issue.getTitle().toLowerCase().contains(value);
		else
			return !issue.getTitle().toLowerCase().contains(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

}
