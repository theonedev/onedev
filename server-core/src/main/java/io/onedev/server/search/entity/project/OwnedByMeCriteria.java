package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public class OwnedByMeCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		if (User.get() != null)
			return new OwnedByCriteria(User.get()).getPredicate(query, from, builder);
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
