package io.onedev.server.search.entity.build;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class FinishedCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<?> attribute = from.get(Build.PROP_STATUS);
		return builder.or(
				builder.equal(attribute, Build.Status.SUCCESSFUL),
				builder.equal(attribute, Build.Status.FAILED),
				builder.equal(attribute, Build.Status.TIMED_OUT),
				builder.equal(attribute, Build.Status.CANCELLED));
	}

	@Override
	public boolean matches(Build build) {
		return build.getStatus() == Build.Status.SUCCESSFUL
				|| build.getStatus() == Build.Status.FAILED
				|| build.getStatus() == Build.Status.TIMED_OUT
				|| build.getStatus() == Build.Status.CANCELLED;
	}

	@Override
	public String toStringWithoutParens() {
		return BuildQuery.getRuleName(BuildQueryLexer.Running);
	}

}
