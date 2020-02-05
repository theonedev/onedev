package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.search.entity.EntityCriteria;

public class ToBeVerifiedByBuildsCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Join<?, ?> join = root
				.join(PullRequest.PROP_PULL_REQUEST_BUILDS, JoinType.LEFT)
				.join(PullRequestBuild.PROP_BUILD, JoinType.INNER);
		Path<?> status = join.get(Build.STATUS);
		join.on(builder.or(
				builder.equal(status, Build.Status.RUNNING), 
				builder.equal(status, Build.Status.PENDING), 
				builder.equal(status, Build.Status.WAITING)));
		
		return join.isNotNull();
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestBuild build: request.getPullRequestBuilds()) {
			if (build.getBuild().getStatus() == Build.Status.RUNNING 
					|| build.getBuild().getStatus() == Build.Status.PENDING 
					|| build.getBuild().getStatus() == Build.Status.WAITING) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String asString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeVerifiedByBuilds);
	}

}
