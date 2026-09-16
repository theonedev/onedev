package io.onedev.server.search.entity.pack;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Pack;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ProjectCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;
	
	private final String projectPath;
	
	private final int operator;

	public ProjectCriteria(String projectPath, int operator) {
		this.projectPath = projectPath;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		var predicate = OneDev.getInstance(ProjectService.class).getPathMatchPredicate(
				builder, from.join(Pack.PROP_PROJECT, JoinType.INNER), projectPath);
		if (operator == PackQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Pack pack) {
		var matches = WildcardUtils.matchPath(projectPath, pack.getProject().getPath());
		if (operator == PackQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Pack.NAME_PROJECT) + " " 
				+ PackQuery.getRuleName(operator) + " " 
				+ quote(projectPath);
	}

}
