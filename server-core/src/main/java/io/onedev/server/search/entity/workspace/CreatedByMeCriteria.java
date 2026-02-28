package io.onedev.server.search.entity.workspace;

import static io.onedev.server.web.translation.Translation._T;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;

public class CreatedByMeCriteria extends CreatedByCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Path<User> attribute = from.get(Workspace.PROP_USER);
			return builder.equal(attribute, User.get());
		} else {
			throw new ExplicitException(_T("Please login to perform this query"));
		}
	}

	@Override
	public User getCreator() {
		return SecurityUtils.getUser();
	}

	@Override
	public boolean matches(Workspace workspace) {
		if (User.get() != null)
			return User.get().equals(workspace.getUser());
		else
			throw new ExplicitException(_T("Please login to perform this query"));
	}

	@Override
	public String toStringWithoutParens() {
		return WorkspaceQuery.getRuleName(WorkspaceQueryLexer.CreatedByMe);
	}

}
