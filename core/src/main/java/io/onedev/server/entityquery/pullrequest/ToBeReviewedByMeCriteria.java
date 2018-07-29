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
import io.onedev.server.security.SecurityUtils;

public class ToBeReviewedByMeCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Join<?, ?> join = context.getJoin(PullRequest.PATH_REVIEWS);
		Path<?> user = EntityQuery.getPath(join, PullRequestReview.PATH_USER);
		Path<?> excludeDate = EntityQuery.getPath(join, PullRequestReview.PATH_EXCLUDE_DATE);
		Path<?> approved = EntityQuery.getPath(join, PullRequestReview.PATH_RESULT_APPROVED);
		return context.getBuilder().and(
				context.getBuilder().equal(user, SecurityUtils.getUser()), 
				context.getBuilder().isNull(excludeDate), 
				context.getBuilder().isNull(approved));
	}

	@Override
	public boolean matches(PullRequest request) {
		PullRequestReview review = request.getReview(SecurityUtils.getUser());
		return review != null && review.getExcludeDate() == null && review.getResult() == null;
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeReviewedByMe);
	}

}
