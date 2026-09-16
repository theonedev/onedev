package io.onedev.server.search.entity.workspace;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.QueryUtils;
import io.onedev.server.util.criteria.Criteria;

public class IssueCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final Issue issue;

	private final String value;

	private final int operator;

	public IssueCriteria(@Nullable Project project, String value, int operator) {
		issue = QueryUtils.getIssue(project, value);
		this.value = value;
		this.operator = operator;
	}

	public IssueCriteria(@Nullable Project project, Issue issue, int operator) {
		this.issue = issue;
		value = issue.getReference().toString(project);
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query,
			From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<Issue> attribute = from.get(Workspace.PROP_ISSUE);
		var predicate = builder.equal(attribute, issue);
		if (operator == WorkspaceQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Workspace workspace) {
		var matches = issue.equals(workspace.getIssue());
		if (operator == WorkspaceQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Workspace.NAME_ISSUE) + " "
				+ WorkspaceQuery.getRuleName(operator) + " "
				+ quote(value);
	}

}
