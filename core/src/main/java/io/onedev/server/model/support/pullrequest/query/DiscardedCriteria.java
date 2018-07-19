package io.onedev.server.model.support.pullrequest.query;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.util.query.QueryBuildContext;

public class DiscardedCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private PullRequestCriteria getCriteria(Project project) {
		return new StateCriteria(CloseInfo.Status.DISCARDED.toString());
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		return getCriteria(project).getPredicate(project, context);
	}

	@Override
	public boolean matches(PullRequest request) {
		return getCriteria(request.getTargetProject()).matches(request);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.Discarded);
	}

}
