package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ForksOfCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String forkedFromPath;
	
	public ForksOfCriteria(String forkedFromPath) {
		this.forkedFromPath = forkedFromPath;
	}

	public String getForkedFromPath() {
		return forkedFromPath;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Subquery<Project> forkedFromQuery = query.subquery(Project.class);
		Root<Project> forkedFromRoot = forkedFromQuery.from(Project.class);
		forkedFromQuery.select(forkedFromRoot);

		ProjectService manager = OneDev.getInstance(ProjectService.class);
		return builder.exists(forkedFromQuery.where(
				builder.equal(from.get(Project.PROP_FORKED_FROM), forkedFromRoot), 
				manager.getPathMatchPredicate(builder, forkedFromRoot, forkedFromPath)));
	}

	@Override
	public boolean matches(Project project) {
		if (project.getForkedFrom() != null)
			return WildcardUtils.matchPath(forkedFromPath, project.getForkedFrom().getPath());
		else
			return false;
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.ForksOf) + " " + Criteria.quote(forkedFromPath);
	}

}
