package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;

public class PendingCriteria extends StatusCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<?> attribute = from.get(Build.PROP_STATUS);
		return builder.equal(attribute, Build.Status.PENDING);
	}

	@Override
	public boolean matches(Build build) {
		return build.getStatus() == Build.Status.PENDING;
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.Pending);
	}

	@Override
	public Build.Status getStatus() {
		return Build.Status.PENDING;
	}

}
