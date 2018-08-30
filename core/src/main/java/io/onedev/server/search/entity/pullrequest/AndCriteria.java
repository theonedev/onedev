package io.onedev.server.search.entity.pullrequest;

import java.util.List;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.AndCriteriaHelper;
import io.onedev.server.search.entity.ParensAware;
import io.onedev.server.search.entity.QueryBuildContext;

public class AndCriteria extends PullRequestCriteria implements ParensAware {
	
	private static final long serialVersionUID = 1L;

	private final List<PullRequestCriteria> criterias;
	
	public AndCriteria(List<PullRequestCriteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		return new AndCriteriaHelper<PullRequest>(criterias).getPredicate(project, context, user);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return new AndCriteriaHelper<PullRequest>(criterias).matches(request, user);
	}

	@Override
	public boolean needsLogin() {
		return new AndCriteriaHelper<PullRequest>(criterias).needsLogin();
	}

	@Override
	public String toString() {
		return new AndCriteriaHelper<PullRequest>(criterias).toString();
	}
	
}
