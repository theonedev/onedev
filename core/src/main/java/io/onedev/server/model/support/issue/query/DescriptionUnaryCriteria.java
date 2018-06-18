package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

public class DescriptionUnaryCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;
	
	public DescriptionUnaryCriteria(int operator) {
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext context) {
		Path<?> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.DESCRIPTION));
		if (operator == IssueQueryLexer.IsEmpty)
			return context.getBuilder().isNull(attribute);
		else
			return context.getBuilder().isNotNull(attribute);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.IsEmpty)
			return issue.getDescription() == null;
		else
			return issue.getDescription() != null;
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.DESCRIPTION) + " " + IssueQuery.getRuleName(operator);
	}

}
