package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;

public class MilestoneUnaryCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;

	public MilestoneUnaryCriteria(int operator) {
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext context) {
		Path<?> attribute = context.getJoin(Issue.MILESTONE).get(Milestone.NAME);
		if (operator == IssueQueryLexer.IsEmpty)
			return context.getBuilder().isNull(attribute);
		else 
			return context.getBuilder().isNotNull(attribute);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.IsEmpty)
			return issue.getMilestone() == null;
		else 
			return issue.getMilestone() != null;
	}

	@Override
	public boolean needsLogin() {
		return operator == IssueQueryLexer.IsMe || operator == IssueQueryLexer.IsNotMe;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.MILESTONE) + " " + IssueQuery.getRuleName(operator);
	}

}
