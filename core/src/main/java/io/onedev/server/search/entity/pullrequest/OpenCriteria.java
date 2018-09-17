package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.PullRequestConstants;
import io.onedev.server.search.entity.QueryBuildContext;

public class OpenCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private PullRequestCriteria getCriteria(Project project) {
		return new StatusCriteria(PullRequestConstants.STATE_OPEN);
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		return getCriteria(project).getPredicate(project, context, user);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return getCriteria(request.getTargetProject()).matches(request, user);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.Open);
	}

}
