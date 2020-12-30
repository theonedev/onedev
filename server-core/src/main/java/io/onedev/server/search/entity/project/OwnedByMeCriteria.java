package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;

public class OwnedByMeCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		if (User.get() != null)
			return new OwnedByCriteria(User.get()).getPredicate(root, builder);
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public boolean matches(Project project) {
		if (User.get() != null)
			return new OwnedByCriteria(User.get()).matches(project);
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.OwnedByMe);
	}

}
