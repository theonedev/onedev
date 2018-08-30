package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.search.entity.QueryBuildContext;

public class ClosedCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private IssueCriteria getCriteria(Project project) {
		return project.getIssueWorkflow().getCategoryCriteria(StateSpec.Category.CLOSED);
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		return getCriteria(project).getPredicate(project, context, user);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return getCriteria(issue.getProject()).matches(issue, user);
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
