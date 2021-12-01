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
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class SomeoneRequestedForChangesCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Join<?, ?> join = from.join(PullRequest.PROP_REVIEWS, JoinType.LEFT);
		Path<?> userPath = EntityQuery.getPath(join, PullRequestReview.PROP_USER);
		Path<?> approvedPath = EntityQuery.getPath(join, PullRequestReview.PROP_RESULT + "." + ReviewResult.PROP_APPROVED);
		join.on(builder.and(
				builder.isNotNull(userPath), 
				builder.equal(approvedPath, false)));
		return join.isNotNull();
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestReview review: request.getReviews()) {
			if (review.getResult() != null && Boolean.FALSE.equals(review.getResult().getApproved()))
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.SomeoneRequestedForChanges);
	}

}
