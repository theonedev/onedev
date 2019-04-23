package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.PullRequestConstants;

public class HasFailedBuildsCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		From<?, ?> join = context.getJoin(PullRequestConstants.ATTR_BUILDS + "." + PullRequestBuild.ATTR_BUILD);
		Path<?> status = join.get(Build.STATUS);
		
		return context.getBuilder().or(
				context.getBuilder().equal(status, Build.Status.IN_ERROR), 
				context.getBuilder().equal(status, Build.Status.FAILED), 
				context.getBuilder().equal(status, Build.Status.CANCELLED));
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		for (PullRequestBuild build: request.getBuilds()) {
			if (build.getBuild() != null && 
					(build.getBuild().getStatus() == Build.Status.IN_ERROR 
					|| build.getBuild().getStatus() == Build.Status.FAILED
					|| build.getBuild().getStatus() == Build.Status.CANCELLED)) {
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
