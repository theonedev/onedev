package io.onedev.server.entityquery.pullrequest;

import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryLexer;

public class MergedCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private PullRequestCriteria getCriteria(Project project) {
		return new StateCriteria(CloseInfo.Status.MERGED.toString());
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
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.Merged);
	}

}
