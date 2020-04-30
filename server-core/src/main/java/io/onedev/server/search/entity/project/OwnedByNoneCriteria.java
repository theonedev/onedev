package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.search.entity.EntityCriteria;

public class OwnedByNoneCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		Join<?, ?> userAuthorizationJoin = root.join(Project.PROP_USER_AUTHORIZATIONS, JoinType.LEFT);
		Join<?, ?> groupAuthorizationJoin = root.join(Project.PROP_GROUP_AUTHORIZATIONS, JoinType.LEFT);

		userAuthorizationJoin.on(builder.equal(
				userAuthorizationJoin.get(UserAuthorization.PROP_ROLE), Role.OWNER_ID));
		groupAuthorizationJoin.on(
				builder.equal(groupAuthorizationJoin.get(GroupAuthorization.PROP_ROLE), Role.OWNER_ID));
		return builder.and(userAuthorizationJoin.isNull(), groupAuthorizationJoin.isNull());
	}

	@Override
	public boolean matches(Project project) {
		for (UserAuthorization authorization: project.getUserAuthorizations()) {
			if (authorization.getRole().isOwner())
				return false;
		}
		for (GroupAuthorization authorization: project.getGroupAuthorizations()) {
			if (authorization.getRole().isOwner())
				return false;;
		}
		return true;
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.OwnedByNone);
	}

}
