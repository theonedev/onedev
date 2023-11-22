package io.onedev.server.search.entity.pack;

import io.onedev.server.model.Pack;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;
import java.util.Objects;

public class PublishedByUserCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public PublishedByUserCriteria(User user) {
		this.user = user;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
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
