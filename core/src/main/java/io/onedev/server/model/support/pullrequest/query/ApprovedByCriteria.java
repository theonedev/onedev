package io.onedev.server.model.support.pullrequest.query;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.util.query.EntityQuery;
import io.onedev.server.util.query.QueryBuildContext;

public class ApprovedByCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final User value;
	
	public ApprovedByCriteria(User value) {
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Join<?, ?> join = context.getJoin(PullRequest.PATH_REVIEWS);
		Path<?> user = EntityQuery.getPath(join, PullRequestReview.PATH_USER);
		Path<?> excludeDate = EntityQuery.getPath(join, PullRequestReview.PATH_EXCLUDE_DATE);
		Path<?> approved = EntityQuery.getPath(join, PullRequestReview.PATH_RESULT_APPROVED);
		return context.getBuilder().and(
				context.getBuilder().equal(user, value), 
				context.getBuilder().isNull(excludeDate), 
				context.getBuilder().equal(approved, true));
	}

	@Override
	public boolean matches(PullRequest request) {
		PullRequestReview review = request.getReview(value);
		return review != null && review.getExcludeDate() == null && review.getResult() != null
				&& review.getResult().isApproved();
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ApprovedBy) + " " + PullRequestQuery.quote(value.getName());
	}

}
