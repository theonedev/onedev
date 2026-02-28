package io.onedev.server.search.entity.workspace;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Workspace;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;

public class CreatedByUserCriteria extends CreatedByCriteria {

	private static final long serialVersionUID = 1L;

	private final User user;

	public CreatedByUserCriteria(User user) {
		this.user = user;
	}

	@Override
	public User getCreator() {
		return user;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<User> attribute = from.get(Workspace.PROP_USER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(Workspace workspace) {
		return Objects.equals(workspace.getUser(), user);
	}

	@Override
	public String toStringWithoutParens() {
		return WorkspaceQuery.getRuleName(WorkspaceQueryLexer.CreatedBy) + " " + quote(user.getName());
	}

}
