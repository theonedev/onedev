package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequest.Status;
import io.onedev.server.util.ProjectScope;

public class DiscardedCriteria extends StatusCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return builder.equal(from.get(PullRequest.PROP_STATUS), Status.DISCARDED);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.isDiscarded();
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.Discarded);
	}

	@Override
	public Status getStatus() {
		return Status.DISCARDED;
	}

}
