package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ChildrenOfCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String parentPath;
	
	public ChildrenOfCriteria(String parentPath) {
		this.parentPath = parentPath;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, Root<Project> root, CriteriaBuilder builder) {
		return OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(
				builder, root.join(Project.PROP_PARENT), parentPath);
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
