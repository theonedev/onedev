package io.onedev.server.search.entity.pack;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Pack;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class PublishedByMeCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Path<User> attribute = from.get(Pack.PROP_USER);
			return builder.equal(attribute, User.get());
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(Pack pack) {
		if (User.get() != null)
			return User.get().equals(pack.getUser());
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return PackQuery.getRuleName(PackQueryLexer.PublishedByMe);
	}

}
