package io.onedev.server.search.entity.workspace;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class BranchEmptyCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final int operator;

	public BranchEmptyCriteria(int operator) {
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		var predicate = builder.isNull(from.get(Workspace.PROP_BRANCH));
		if (operator == WorkspaceQueryLexer.IsNotEmpty)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Workspace workspace) {
		var matches = workspace.getBranch() == null;
		if (operator == WorkspaceQueryLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Workspace.NAME_BRANCH) + " " + WorkspaceQuery.getRuleName(operator);
	}

}
