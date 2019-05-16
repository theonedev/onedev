package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.CloseInfo;

public class MergedCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private PullRequestCriteria getCriteria(Project project) {
		return new StatusCriteria(CloseInfo.Status.MERGED.toString());
	}
	
	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		return getCriteria(project).getPredicate(project, root, builder, user);
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
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.Merged);
	}

}
