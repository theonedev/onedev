package io.onedev.server.entityquery.pullrequest;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryLexer;

public class HasFailedBuildsCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Join<?, ?> join = context.getJoin(PullRequest.PATH_BUILDS + "." + PullRequestBuild.PATH_BUILD);
		Path<?> status = join.get(Build.STATUS);
		
		return context.getBuilder().or(
				context.getBuilder().equal(status, Build.Status.ERROR), 
				context.getBuilder().equal(status, Build.Status.FAILURE));
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestBuild build: request.getBuilds()) {
			if (build.getBuild() != null && 
					(build.getBuild().getStatus() == Build.Status.ERROR || build.getBuild().getStatus() == Build.Status.FAILURE)) {
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
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasFailedBuilds);
	}

}
