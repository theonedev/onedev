package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

public class CommentsCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public CommentsCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext context) {
		Path<Integer> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.COMMENTS));
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else if (operator == IssueQueryLexer.IsNot)
			return context.getBuilder().notEqual(attribute, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return context.getBuilder().greaterThan(attribute, value);
		else
			return context.getBuilder().lessThan(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getNumOfComments() == value;
		else if (operator == IssueQueryLexer.IsNot)
			return issue.getNumOfComments() != value;
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getNumOfComments() > value;
		else
			return issue.getNumOfComments() < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.COMMENTS) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(String.valueOf(value));
	}

}
