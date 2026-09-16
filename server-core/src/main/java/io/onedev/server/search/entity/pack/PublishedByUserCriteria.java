package io.onedev.server.search.entity.pack;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Pack;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;

public class PublishedByUserCriteria extends PublishedByCriteria {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public PublishedByUserCriteria(User user) {
		this.user = user;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		Path<User> attribute = from.get(Pack.PROP_USER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(Pack pack) {
		return Objects.equals(pack.getUser(), user);
	}

	@Override
	public String toStringWithoutParens() {
		return PackQuery.getRuleName(PackQueryLexer.PublishedByUser) + " " + quote(user.getName());
	}

}
