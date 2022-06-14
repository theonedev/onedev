package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class ToBeReviewedByMeCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Join<?, ?> join = from.join(PullRequest.PROP_REVIEWS, JoinType.LEFT);
			Path<?> userPath = EntityQuery.getPath(join, PullRequestReview.PROP_USER);
			Path<?> statusPath = EntityQuery.getPath(join, PullRequestReview.PROP_STATUS);
			join.on(builder.and(
					builder.equal(userPath, User.get()), 
					builder.equal(statusPath, Status.PENDING)));
			return join.isNotNull();
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(PullRequest request) {
		if (User.get() != null) {
			PullRequestReview review = request.getReview(User.get());
			return review != null && review.getStatus() == Status.PENDING;
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeReviewedByMe);
	}

}
