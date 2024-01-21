package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class ToBeReviewedByCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public ToBeReviewedByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestReview> reviewQuery = query.subquery(PullRequestReview.class);
		Root<PullRequestReview> review = reviewQuery.from(PullRequestReview.class);
		reviewQuery.select(review);
		reviewQuery.where(builder.and(
				builder.equal(review.get(PullRequestReview.PROP_STATUS), Status.PENDING),
				builder.equal(review.get(PullRequestReview.PROP_REQUEST), from),
				builder.equal(review.get(PullRequestReview.PROP_USER), user)));
		return builder.and(
				builder.equal(from.get(PullRequest.PROP_STATUS), PullRequest.Status.OPEN), 
				builder.exists(reviewQuery));
	}

	@Override
	public boolean matches(PullRequest request) {
		PullRequestReview review = request.getReview(user);
		return request.isOpen() && review != null && review.getStatus() == Status.PENDING;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeReviewedBy) + " " + quote(user.getName());
	}

}
