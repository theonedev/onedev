package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;

public class ForkRootsCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		return builder.isNull(root.get(Project.PROP_FORKED_FROM));
	}

	@Override
	public boolean matches(Project project) {
		return project.getForkedFrom() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.ForkRoots);
	}

}
