package io.onedev.server.entityquery.pullrequest;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.EntityQuery;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.support.pullrequest.PullRequestConstants;

public class HasPendingReviewsCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		From<?, ?> join = context.getJoin(PullRequestConstants.ATTR_REVIEWS);
		Path<?> user = EntityQuery.getPath(join, PullRequestReview.ATTR_USER);
		Path<?> excludeDate = EntityQuery.getPath(join, PullRequestReview.ATTR_EXCLUDE_DATE);
		Path<?> approved = EntityQuery.getPath(join, PullRequestReview.ATTR_RESULT_APPROVED);
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
