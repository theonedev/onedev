package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.query.QueryBuildContext;

public class CommentCountCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public CommentCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Path<Integer> attribute = context.getRoot().get(Issue.FIELD_PATHS.get(Issue.FIELD_COMMENT_COUNT));
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return context.getBuilder().greaterThan(attribute, value);
		else
			return context.getBuilder().lessThan(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getCommentCount() == value;
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getCommentCount() > value;
		else
			return issue.getCommentCount() < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.FIELD_COMMENT_COUNT) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(String.valueOf(value));
	}

}
