package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.*;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class HasPendingReviewsCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestReview> reviewQuery = query.subquery(PullRequestReview.class);
		Root<PullRequestReview> review = reviewQuery.from(PullRequestReview.class);
		reviewQuery.select(review);
		reviewQuery.where(builder.and(
				builder.equal(review.get(PullRequestReview.PROP_STATUS), Status.PENDING),
				builder.equal(review.get(PullRequestReview.PROP_REQUEST), from)));
		return builder.and(
				builder.equal(from.get(PullRequest.PROP_STATUS), PullRequest.Status.OPEN),
				builder.exists(reviewQuery));
	}

	@Override
	public boolean matches(PullRequest request) {
		if (request.isOpen()) {
			for (PullRequestReview review : request.getReviews()) {
				if (review.getStatus() == Status.PENDING)
					return true;
			}
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasPendingReviews);
	}

}
