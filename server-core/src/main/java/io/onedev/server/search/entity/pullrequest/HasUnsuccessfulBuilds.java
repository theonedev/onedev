package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class HasUnsuccessfulBuilds extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<Build> buildQuery = query.subquery(Build.class);
		Root<Build> build = buildQuery.from(Build.class);
		buildQuery.select(build);
		var status = build.get(Build.PROP_STATUS);
		var buildCommitOfPullRequest = PullRequestQuery.getPath(from, PullRequest.PROP_BUILD_COMMIT_HASH);
		var buildCommit = build.get(Build.PROP_COMMIT_HASH);
		buildQuery.where(builder.and(
				builder.or(
						builder.equal(status, Build.Status.FAILED),
						builder.equal(status, Build.Status.CANCELLED),
						builder.equal(status, Build.Status.TIMED_OUT)),
				builder.equal(build.get(Build.PROP_PULL_REQUEST), from),
				builder.equal(buildCommitOfPullRequest, buildCommit)));
		return builder.and(
				builder.equal(from.get(PullRequest.PROP_STATUS), PullRequest.Status.OPEN),
				builder.exists(buildQuery));
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
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasUnsuccessfulBuilds);
	}

}
