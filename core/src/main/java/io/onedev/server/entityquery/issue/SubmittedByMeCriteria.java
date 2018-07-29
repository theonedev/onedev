package io.onedev.server.entityquery.issue;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.entityquery.issue.IssueQueryLexer;
import io.onedev.server.security.SecurityUtils;

public class SubmittedByMeCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Path<User> attribute = context.getRoot().get(Issue.FIELD_PATHS.get(Issue.FIELD_SUBMITTER));
		return context.getBuilder().equal(attribute, SecurityUtils.getUser());
	}

	@Override
	public boolean matches(Issue issue) {
		return Objects.equals(issue.getSubmitter(), SecurityUtils.getUser());
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.SubmittedByMe);
	}

}
