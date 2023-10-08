package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final String projectPath;

	public ProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		return OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(
				builder, from.join(Build.PROP_PROJECT, JoinType.INNER), projectPath);
	}

	@Override
	public boolean matches(Build build) {
		return WildcardUtils.matchPath(projectPath, build.getProject().getPath());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_PROJECT) + " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) + " " 
				+ quote(projectPath);
	}

}
