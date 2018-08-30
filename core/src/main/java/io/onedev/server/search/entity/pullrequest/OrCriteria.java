package io.onedev.server.search.entity.pullrequest;

import java.util.List;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.OrCriteriaHelper;
import io.onedev.server.search.entity.ParensAware;
import io.onedev.server.search.entity.QueryBuildContext;

public class OrCriteria extends PullRequestCriteria implements ParensAware {
	
	private static final long serialVersionUID = 1L;

	private final List<PullRequestCriteria> criterias;
	
	public OrCriteria(List<PullRequestCriteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		return new OrCriteriaHelper<PullRequest>(criterias).getPredicate(project, context, user);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return new OrCriteriaHelper<PullRequest>(criterias).matches(request, user);
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
