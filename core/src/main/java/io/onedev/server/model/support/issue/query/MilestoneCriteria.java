package io.onedev.server.model.support.issue.query;

import java.util.Set;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;

public class MilestoneCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;
	
	private final String value;

	public MilestoneCriteria(String value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	public int getOperator() {
		return operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext context) {
		Path<?> attribute = context.getJoin(Issue.MILESTONE).get(Milestone.NAME);
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else 
			return context.getBuilder().notEqual(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getMilestone() != null && issue.getMilestone().getName().equals(value);
		else 
			return issue.getMilestone() == null || !issue.getMilestone().getName().equals(value);
	}

	@Override
	public boolean needsLogin() {
		return operator == IssueQueryLexer.IsMe || operator == IssueQueryLexer.IsNotMe;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.MILESTONE) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(value);
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (operator == IssueQueryLexer.Is)
			issue.setMilestone(issue.getProject().getMilestone(value));
	}

}
