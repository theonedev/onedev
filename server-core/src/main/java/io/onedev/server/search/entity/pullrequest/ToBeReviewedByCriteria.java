package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class ToBeReviewedByCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public ToBeReviewedByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Join<?, ?> join = from.join(PullRequest.PROP_REVIEWS, JoinType.LEFT);
		Path<?> userPath = EntityQuery.getPath(join, PullRequestReview.PROP_USER);
		Path<?> approvedPath = EntityQuery.getPath(join, PullRequestReview.PROP_RESULT + "." + ReviewResult.PROP_APPROVED);
		join.on(builder.and(
				builder.equal(userPath, user), 
				builder.isNull(approvedPath)));
		return join.isNotNull();
	}

	@Override
	public boolean matches(PullRequest request) {
		PullRequestReview review = request.getReview(user);
		return review != null && review.getResult() == null || review.getResult().getApproved() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeReviewedBy) + " " + quote(user.getName());
	}

}
