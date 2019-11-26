package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectConstants;

public class ForksOfCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final String value;
	
	public ForksOfCriteria(String value) {
		project = EntityQuery.getProject(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder, User user) {
		return builder.equal(root.get(ProjectConstants.ATTR_FORKED_FROM), project);
	}

	@Override
	public boolean matches(Project project, User user) {
		return this.project.equals(project.getForkedFrom());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.ForksOf) + " " + ProjectQuery.quote(value);
	}

}
