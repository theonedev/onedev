package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.criteria.Criteria;

public class ChildrenOfCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	public ChildrenOfCriteria(Project project) {
		this.project = project;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		return builder.equal(root.get(Project.PROP_PARENT), project);
	}

	@Override
	public boolean matches(Project project) {
		return this.project.equals(project.getParent());
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.ChildrenOf) + " " + Criteria.quote(project.getPath());
	}

}
