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

public class ChildrenOfCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String parentPath;
	
	public ChildrenOfCriteria(String parentPath) {
		this.parentPath = parentPath;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Subquery<Project> parentQuery = query.subquery(Project.class);
		Root<Project> parentRoot = parentQuery.from(Project.class);
		parentQuery.select(parentRoot);

		ProjectManager manager = OneDev.getInstance(ProjectManager.class);
		return builder.exists(parentQuery.where(
				builder.equal(from.get(Project.PROP_PARENT), parentRoot), 
				manager.getPathMatchPredicate(builder, parentRoot, parentPath)));
	}

	@Override
	public boolean matches(Project project) {
		if (project.getParent() != null)
			return WildcardUtils.matchPath(parentPath, project.getParent().getPath());
		else
			return false;
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.ChildrenOf) + " " + Criteria.quote(parentPath);
	}

}
