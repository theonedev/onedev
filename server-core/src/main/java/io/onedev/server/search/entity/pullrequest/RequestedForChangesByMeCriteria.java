package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class RequestedForChangesByMeCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder, User user) {
		if (user != null) {
			From<?, ?> join = root.join(PullRequestQueryConstants.ATTR_REVIEWS, JoinType.LEFT);
			Path<?> userPath = EntityQuery.getPath(join, PullRequestReview.ATTR_USER);
			Path<?> excludeDatePath = EntityQuery.getPath(join, PullRequestReview.ATTR_EXCLUDE_DATE);
			Path<?> approvedPath = EntityQuery.getPath(join, PullRequestReview.ATTR_RESULT_APPROVED);
			return builder.and(
					builder.equal(userPath, user), 
					builder.isNull(excludeDatePath), 
					builder.equal(approvedPath, false));
		} else {
			return builder.disjunction();
		}
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		PullRequestReview review = request.getReview(user);
		if (user != null) {
			return review != null 
					&& review.getExcludeDate() == null 
					&& review.getResult() != null 
					&& !review.getResult().isApproved();
		} else {
			return false;
		}
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.RequestedForChangesByMe);
	}

}
