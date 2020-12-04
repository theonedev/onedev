package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.search.entity.EntityCriteria;

public class PullRequestIsEmptyCriteria extends EntityCriteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<Build> root, CriteriaBuilder builder) {
		return builder.isNull(root.get(Build.PROP_PULL_REQUEST));
	}

	@Override
	public boolean matches(Build build) {
		return build.getRequest() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_PULL_REQUEST) + " " + BuildQuery.getRuleName(BuildQueryLexer.IsEmpty);
	}

}
