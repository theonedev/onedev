package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ForksOfCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String forkedFromPath;
	
	public ForksOfCriteria(String forkedFromPath) {
		this.forkedFromPath = forkedFromPath;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		return OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(
				builder, root.join(Project.PROP_FORKED_FROM), forkedFromPath);
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
