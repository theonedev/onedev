package io.onedev.server.search.entity.build;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class CancelledByUserCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public CancelledByUserCriteria(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<User> attribute = from.get(Build.PROP_CANCELLER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(Build build) {
		return Objects.equals(build.getCanceller(), user);
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.CancelledBy) + " " + quote(user.getName());
	}

}
