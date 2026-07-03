package io.onedev.server.search.entity.workspace;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Workspace;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class PullRequestCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final PullRequest pullRequest;

	private final String value;

	private final int operator;

	public PullRequestCriteria(@Nullable Project project, String value, int operator) {
		pullRequest = EntityQuery.getPullRequest(project, value);
		this.value = value;
		this.operator = operator;
	}

	public PullRequestCriteria(@Nullable Project project, PullRequest pullRequest, int operator) {
		this.pullRequest = pullRequest;
		value = pullRequest.getReference().toString(project);
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query,
			From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<PullRequest> attribute = from.get(Workspace.PROP_PULL_REQUEST);
		var predicate = builder.equal(attribute, pullRequest);
		if (operator == WorkspaceQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Workspace workspace) {
		var matches = pullRequest.equals(workspace.getRequest());
		if (operator == WorkspaceQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Workspace.NAME_PULL_REQUEST) + " "
				+ WorkspaceQuery.getRuleName(operator) + " "
				+ quote(value);
	}

}
