package io.onedev.server.model.support.pullrequest.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.query.QueryBuildContext;

public class PullRequestQueryBuildContext extends QueryBuildContext<PullRequest> {
	
	public PullRequestQueryBuildContext(Root<PullRequest> root, CriteriaBuilder builder) {
		super(root, builder);
	}
	
	@Override
	public Join<?, ?> newJoin(String joinPath) {
		if (joinPath.startsWith(PullRequest.PATH_BUILDS + ".")) {
			Join<?, ?> join = getRoot().join(PullRequest.PATH_BUILDS, JoinType.LEFT);
			return join.join(joinPath.substring((PullRequest.PATH_BUILDS+".").length()), JoinType.LEFT);
		} else if (joinPath.startsWith(PullRequest.PATH_REVIEWS + ".")) {
			Join<?, ?> join = getRoot().join(PullRequest.PATH_REVIEWS, JoinType.LEFT);
			return join.join(joinPath.substring((PullRequest.PATH_REVIEWS+".").length()), JoinType.LEFT);
		} else {
			return getRoot().join(joinPath, JoinType.LEFT);
		}
	}

}
