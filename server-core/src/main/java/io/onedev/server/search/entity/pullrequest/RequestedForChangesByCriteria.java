package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class RequestedForChangesByCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public RequestedForChangesByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestReview> reviewQuery = query.subquery(PullRequestReview.class);
		Root<PullRequestReview> review = reviewQuery.from(PullRequestReview.class);
		reviewQuery.select(review);
		reviewQuery.where(builder.and(
				builder.equal(review.get(PullRequestReview.PROP_STATUS), Status.REQUESTED_FOR_CHANGES),
				builder.equal(review.get(PullRequestReview.PROP_REQUEST), from),
				builder.equal(review.get(PullRequestReview.PROP_USER), user)));
		return builder.exists(reviewQuery);
	}

	@Override
	public boolean matches(PullRequest request) {
		PullRequestReview review = request.getReview(this.user);
		return review != null && review.getStatus() == Status.REQUESTED_FOR_CHANGES;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.RequestedForChangesBy) + " " + quote(user.getName());
	}

}
