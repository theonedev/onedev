package io.onedev.server.entityquery.pullrequest;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.EntityQuery;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryLexer;

public class HasPendingReviewsCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Join<?, ?> join = context.getJoin(PullRequest.PATH_REVIEWS);
		Path<?> user = EntityQuery.getPath(join, PullRequestReview.PATH_USER);
		Path<?> excludeDate = EntityQuery.getPath(join, PullRequestReview.PATH_EXCLUDE_DATE);
		Path<?> approved = EntityQuery.getPath(join, PullRequestReview.PATH_RESULT_APPROVED);
		return context.getBuilder().and(
				context.getBuilder().isNotNull(user), 
				context.getBuilder().isNull(excludeDate), 
				context.getBuilder().isNull(approved));
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestReview review: request.getReviews()) {
			if (review.getExcludeDate() == null && review.getResult() == null)
				return true;
		}
		return false;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasPendingReviews);
	}

}
