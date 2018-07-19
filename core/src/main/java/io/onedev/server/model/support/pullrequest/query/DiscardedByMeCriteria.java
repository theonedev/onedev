package io.onedev.server.model.support.pullrequest.query;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.query.QueryBuildContext;

public class DiscardedByMeCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<User> attribute = PullRequestQuery.getPath(context.getRoot(), PullRequest.PATH_CLOSE_USER);
		return context.getBuilder().equal(attribute, SecurityUtils.getUser());
	}

	@Override
	public boolean matches(PullRequest request) {
		return Objects.equals(request.getSubmitter(), SecurityUtils.getUser());
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.DiscardedByMe);
	}

}
