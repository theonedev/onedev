package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.BuildQueryConstants;

public class VersionIsEmptyCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		return builder.isNull(root.get(BuildQueryConstants.ATTR_VERSION));
	}

	@Override
	public boolean matches(Build build) {
		return build.getVersion() == null;
	}

	@Override
	public String asString() {
		return quote(BuildQueryConstants.FIELD_VERSION) + " " + BuildQuery.getRuleName(BuildQueryLexer.IsEmpty);
	}

}
