package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.PullRequestConstants;

public class RequestForChangesByCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final User value;
	
	public RequestForChangesByCriteria(User value) {
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		From<?, ?> join = context.getJoin(PullRequestConstants.ATTR_REVIEWS);
		Path<?> userPath = EntityQuery.getPath(join, PullRequestReview.ATTR_USER);
		Path<?> excludeDatePath = EntityQuery.getPath(join, PullRequestReview.ATTR_EXCLUDE_DATE);
		Path<?> approvedPath = EntityQuery.getPath(join, PullRequestReview.ATTR_RESULT_APPROVED);
		return context.getBuilder().and(
				context.getBuilder().equal(userPath, value), 
				context.getBuilder().isNull(excludeDatePath), 
				context.getBuilder().equal(approvedPath, false));
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		PullRequestReview review = request.getReview(value);
		return review != null && review.getExcludeDate() == null && review.getResult() != null
				&& !review.getResult().isApproved();
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.RequestedForChangesBy) + " " + PullRequestQuery.quote(value.getName());
	}

}
