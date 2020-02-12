package io.onedev.server.search.entity.build;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class CancelledByCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	private final String value;
	
	public CancelledByCriteria(String value) {
		user = EntityQuery.getUser(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		Path<User> attribute = root.get(Build.PROP_CANCELLER);
		return builder.equal(attribute, this.user);
	}

	@Override
	public boolean matches(Build build) {
		return Objects.equals(build.getCanceller(), this.user);
	}

	@Override
	public String asString() {
		return BuildQuery.getRuleName(BuildQueryLexer.CancelledBy) + " " + quote(value);
	}

}
