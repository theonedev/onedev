package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.ProjectQueryConstants;

public class OwnedByMeCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		Expression<String> attribute = root.get(ProjectQueryConstants.ATTR_OWNER);
		if (User.get() != null)
			return builder.equal(attribute, User.get());
		else
			throw new OneException("Please login to perform this query");
	}

	@Override
	public boolean matches(Project project) {
		if (User.get() != null)
			return User.get().equals(project.getOwner());
		else
			throw new OneException("Please login to perform this query");
	}

	@Override
	public String asString() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.OwnedByMe);
	}

}
