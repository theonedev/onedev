package io.onedev.server.search.entity.build;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

public class CancelledByCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public CancelledByCriteria(User user) {
		this.user = user;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
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
