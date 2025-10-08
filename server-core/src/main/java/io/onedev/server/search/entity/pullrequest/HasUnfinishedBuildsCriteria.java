package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class HasUnfinishedBuildsCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<Build> buildQuery = query.subquery(Build.class);
		Root<Build> build = buildQuery.from(Build.class);
		buildQuery.select(build);
		var status = build.get(Build.PROP_STATUS);
		var buildCommitOfPullRequest = PullRequestQuery.getPath(from, PullRequest.PROP_BUILD_COMMIT_HASH);
		var buildCommit = build.get(Build.PROP_COMMIT_HASH);
		buildQuery.where(builder.and(
				builder.or(
						builder.equal(status, Build.Status.RUNNING),
						builder.equal(status, Build.Status.PENDING),
						builder.equal(status, Build.Status.WAITING)),
				builder.equal(build.get(Build.PROP_PULL_REQUEST), from),
				builder.equal(buildCommitOfPullRequest, buildCommit)));
		return builder.and(
				builder.equal(from.get(PullRequest.PROP_STATUS), PullRequest.Status.OPEN),
				builder.exists(buildQuery));
	}

	@Override
	public boolean matches(PullRequest request) {
		if (request.isOpen()) {
			for (Build build : request.getCurrentBuilds()) {
				if (build.getStatus() == Build.Status.RUNNING
						|| build.getStatus() == Build.Status.PENDING
						|| build.getStatus() == Build.Status.WAITING) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasUnfinishedBuilds);
	}

}
