package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.NotCriteriaHelper;
import io.onedev.server.search.entity.QueryBuildContext;

public class NotCriteria extends PullRequestCriteria {
	
	private static final long serialVersionUID = 1L;

	private final PullRequestCriteria criteria;
	
	public NotCriteria(PullRequestCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		return new NotCriteriaHelper<PullRequest>(criteria).getPredicate(project, context, user);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return new NotCriteriaHelper<PullRequest>(criteria).matches(request, user);
	}

	@Override
	public boolean needsLogin() {
		return new NotCriteriaHelper<PullRequest>(criteria).needsLogin();
	}

	@Override
	public String toString() {
		return new NotCriteriaHelper<PullRequest>(criteria).toString();
	}
	
}
