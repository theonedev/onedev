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

public class HasFailedBuildsCriteria extends EntityCriteria<PullRequest> {

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
						builder.equal(status, Build.Status.FAILED), 
						builder.equal(status, Build.Status.CANCELLED), 
						builder.equal(status, Build.Status.TIMED_OUT))));
		return join.isNotNull();
	}

	@Override
	public boolean matches(PullRequest request) {
		for (Build build: request.getCurrentBuilds()) {
			if (build.getStatus() == Build.Status.FAILED
					|| build.getStatus() == Build.Status.CANCELLED
					|| build.getStatus() == Build.Status.TIMED_OUT) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasFailedBuilds);
	}

}
