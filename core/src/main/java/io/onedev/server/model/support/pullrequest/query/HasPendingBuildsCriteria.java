package io.onedev.server.model.support.pullrequest.query;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.util.query.QueryBuildContext;

public class HasPendingBuildsCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Join<?, ?> join = context.getJoin(PullRequest.PATH_BUILDS + "." + PullRequestBuild.PATH_BUILD);
		Path<?> status = join.get(Build.STATUS);
		
		return context.getBuilder().or(
				context.getBuilder().isNull(status), 
				context.getBuilder().equal(status, Build.Status.RUNNING));
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestBuild build: request.getBuilds()) {
			if (build.getBuild() == null || build.getBuild().getStatus() == Build.Status.RUNNING)
				return true;
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
