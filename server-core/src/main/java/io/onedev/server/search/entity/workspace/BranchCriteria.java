package io.onedev.server.search.entity.workspace;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class BranchCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final String value;

	private final int operator;

	public BranchCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Workspace.PROP_BRANCH);
		String normalized = value.toLowerCase().replace("*", "%");
		Join<?, ?> request = from.join(Workspace.PROP_PULL_REQUEST, JoinType.LEFT);
		var predicate = builder.and(
				builder.like(builder.lower(attribute), normalized),
				builder.or(
						builder.isNull(from.get(Workspace.PROP_PULL_REQUEST)),
						builder.equal(from.get(Workspace.PROP_PROJECT), request.get(PullRequest.PROP_SOURCE_PROJECT))));
		if (operator == WorkspaceQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Workspace workspace) {
		var matches = workspace.getBranch() != null 
				&& (workspace.getRequest() == null || workspace.getProject().equals(workspace.getRequest().getSourceProject())) 
				&& WildcardUtils.matchString(value.toLowerCase(), workspace.getBranch().toLowerCase());
		if (operator == WorkspaceQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Workspace.NAME_BRANCH) + " "
				+ WorkspaceQuery.getRuleName(operator) + " "
				+ quote(value);
	}

}
