package io.onedev.server.entityquery.issue;

import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.entityquery.issue.IssueQueryLexer;
import io.onedev.server.model.support.issue.workflow.StateSpec;

public class ClosedCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private IssueCriteria getCriteria(Project project) {
		return project.getIssueWorkflow().getCategoryCriteria(StateSpec.Category.CLOSED);
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		return getCriteria(project).getPredicate(project, context);
	}

	@Override
	public boolean matches(Issue issue) {
		return getCriteria(issue.getProject()).matches(issue);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.Closed);
	}

}
