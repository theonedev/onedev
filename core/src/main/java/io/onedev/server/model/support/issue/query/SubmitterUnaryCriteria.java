package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.security.SecurityUtils;

public class SubmitterUnaryCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;
	
	public SubmitterUnaryCriteria(int operator) {
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		Path<?> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.SUBMITTER));
		if (operator == IssueQueryLexer.IsMe)
			return context.getBuilder().equal(attribute, SecurityUtils.getUser());
		else
			return context.getBuilder().notEqual(attribute, SecurityUtils.getUser());
	}

}
