package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.search.entity.EntityCriteria;

public class VersionIsEmptyCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		return builder.isNull(from.get(Build.PROP_VERSION));
	}

	@Override
	public boolean matches(Build build) {
		return build.getVersion() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_VERSION) + " " + BuildQuery.getRuleName(BuildQueryLexer.IsEmpty);
	}

}
