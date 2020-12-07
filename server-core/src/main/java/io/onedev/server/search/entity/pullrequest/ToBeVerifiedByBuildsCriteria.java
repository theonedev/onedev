package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.search.entity.EntityCriteria;

public class ToBeVerifiedByBuildsCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(PullRequest.PROP_BUILDS, JoinType.LEFT);
		Path<?> status = join.get(Build.PROP_STATUS);
		
		Path<?> mergeCommit = PullRequestQuery.getPath(root, PullRequest.PROP_LAST_MERGE_PREVIEW + "." + MergePreview.PROP_MERGED_COMMIT_HASH);
		Path<?> buildCommit = join.get(Build.PROP_COMMIT);
		
		join.on(builder.and(
				builder.equal(mergeCommit, buildCommit),
				builder.or(
						builder.equal(status, Build.Status.RUNNING), 
						builder.equal(status, Build.Status.PENDING), 
						builder.equal(status, Build.Status.WAITING))));
		
		return join.isNotNull();
	}

	@Override
	public boolean matches(PullRequest request) {
		for (Build build: request.getBuilds()) {
			if (build.getStatus() == Build.Status.RUNNING 
					|| build.getStatus() == Build.Status.PENDING 
					|| build.getStatus() == Build.Status.WAITING) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeVerifiedByBuilds);
	}

}
