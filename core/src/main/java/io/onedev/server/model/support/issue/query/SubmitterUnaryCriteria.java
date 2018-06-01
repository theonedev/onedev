package io.onedev.server.model.support.issue.query;

import java.util.Objects;

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

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.IsMe)
			return Objects.equals(issue.getSubmitter(), SecurityUtils.getUser());
		else
			return !Objects.equals(issue.getSubmitter(), SecurityUtils.getUser());
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.SUBMITTER) + " " + IssueQuery.getOperatorName(operator);
	}

}
