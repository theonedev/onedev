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
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class RequestedForChangesByCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public RequestedForChangesByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Join<?, ?> join = from.join(PullRequest.PROP_REVIEWS, JoinType.LEFT);
		Path<?> userPath = EntityQuery.getPath(join, PullRequestReview.PROP_USER);
		Path<?> statusPath = EntityQuery.getPath(join, PullRequestReview.PROP_STATUS);
		join.on(builder.and(
				builder.equal(userPath, user), 
				builder.equal(statusPath, Status.REQUESTED_FOR_CHANGES)));
		return join.isNotNull();
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
