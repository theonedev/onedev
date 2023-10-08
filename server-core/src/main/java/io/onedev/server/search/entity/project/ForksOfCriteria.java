package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ForksOfCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String forkedFromPath;
	
	public ForksOfCriteria(String forkedFromPath) {
		this.forkedFromPath = forkedFromPath;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Subquery<Project> forkedFromQuery = query.subquery(Project.class);
		Root<Project> forkedFromRoot = forkedFromQuery.from(Project.class);
		forkedFromQuery.select(forkedFromRoot);

		ProjectManager manager = OneDev.getInstance(ProjectManager.class);
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
