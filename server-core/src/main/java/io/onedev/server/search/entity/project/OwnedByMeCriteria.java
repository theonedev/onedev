package io.onedev.server.search.entity.project;

import static io.onedev.server.web.translation.Translation._T;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;

public class OwnedByMeCriteria extends OwnedByCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		if (User.get() != null)
			return new OwnedByUserCriteria(User.get()).getPredicate(projectScope, query, from, builder);
		else
			throw new ExplicitException(_T("Please login to perform this query"));
	}

	@Override
	public User getUser() {
		return SecurityUtils.getUser();
	}

	@Override
	public boolean matches(Project project) {
		if (User.get() != null)
			return new OwnedByUserCriteria(User.get()).matches(project);
		else
			throw new ExplicitException(_T("Please login to perform this query"));
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.OwnedByMe);
	}

}
