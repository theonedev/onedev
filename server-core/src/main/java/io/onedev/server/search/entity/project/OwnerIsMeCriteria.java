package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.ProjectQueryConstants;

public class OwnerIsMeCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder, User user) {
		Expression<String> attribute = root.get(ProjectQueryConstants.ATTR_OWNER);
		if (user != null)
			return builder.equal(attribute, user);
		else
			return builder.disjunction();
	}

	@Override
	public boolean matches(Project project, User user) {
		if (user != null)
			return user.equals(project.getOwner());
		else
			return false;
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return ProjectQuery.quote(ProjectQueryConstants.FIELD_OWNER) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.IsMe);
	}

}
