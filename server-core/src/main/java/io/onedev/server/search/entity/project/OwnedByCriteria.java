package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.ProjectQueryConstants;

public class OwnedByCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	private final String value;
	
	public OwnedByCriteria(String value) {
		user = EntityQuery.getUser(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		Expression<String> attribute = root.get(ProjectQueryConstants.ATTR_OWNER);
		return builder.equal(attribute, this.user);
	}

	@Override
	public boolean matches(Project project) {
		return project.getOwner().equals(this.user);
	}

	@Override
	public String toString() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.OwnedBy) + " " + ProjectQuery.quote(value);
	}

}
