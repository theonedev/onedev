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

public class OwnerCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	private final String value;
	
	public OwnerCriteria(String value) {
		user = EntityQuery.getUser(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder, User user) {
		Expression<String> attribute = root.get(ProjectQueryConstants.ATTR_OWNER);
		return builder.equal(attribute, this.user);
	}

	@Override
	public boolean matches(Project project, User user) {
		return project.getOwner().equals(this.user);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return ProjectQuery.quote(ProjectQueryConstants.FIELD_OWNER) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.Is) + " " + ProjectQuery.quote(value);
	}

}
