package io.onedev.server.entityquery.pullrequest;

import java.util.List;

import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.OrCriteriaHelper;
import io.onedev.server.entityquery.ParensAware;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;

public class OrCriteria extends PullRequestCriteria implements ParensAware {
	
	private static final long serialVersionUID = 1L;

	private final List<PullRequestCriteria> criterias;
	
	public OrCriteria(List<PullRequestCriteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		return new OrCriteriaHelper<PullRequest>(criterias).getPredicate(project, context);
	}

	@Override
	public boolean matches(PullRequest request) {
		return new OrCriteriaHelper<PullRequest>(criterias).matches(request);
	}

	@Override
	public boolean needsLogin() {
		return new OrCriteriaHelper<PullRequest>(criterias).needsLogin();
	}

	@Override
	public String toString() {
		return new OrCriteriaHelper<PullRequest>(criterias).toString();
	}
	
}
