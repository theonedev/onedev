package io.onedev.server.search.entity.workspace;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ProjectCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final String projectPath;

	private final int operator;

	public ProjectCriteria(String projectPath, int operator) {
		this.projectPath = projectPath;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		var predicate = OneDev.getInstance(ProjectService.class).getPathMatchPredicate(
				builder, from.join(Workspace.PROP_PROJECT, JoinType.INNER), projectPath);
		if (operator == WorkspaceQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Workspace workspace) {
		var matches = WildcardUtils.matchPath(projectPath, workspace.getProject().getPath());
		if (operator == WorkspaceQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Workspace.NAME_PROJECT) + " "
				+ WorkspaceQuery.getRuleName(operator) + " "
				+ quote(projectPath);
	}

}
