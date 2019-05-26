package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class HasPendingBuildsCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		From<?, ?> join = root
				.join(PullRequestConstants.ATTR_BUILDS, JoinType.LEFT)
				.join(PullRequestBuild.ATTR_BUILD, JoinType.LEFT);
		
		Path<?> status = join.get(Build.STATUS);
		
		return builder.or(
				builder.isNull(status), 
				builder.equal(status, Build.Status.RUNNING), 
				builder.equal(status, Build.Status.QUEUEING), 
				builder.equal(status, Build.Status.WAITING));
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		for (PullRequestBuild build: request.getPullRequestBuilds()) {
			if (build.getBuild() == null 
					|| build.getBuild().getStatus() == Build.Status.RUNNING 
					|| build.getBuild().getStatus() == Build.Status.QUEUEING 
					|| build.getBuild().getStatus() == Build.Status.WAITING) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasPendingBuilds);
	}

}
