package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.PullRequestConstants;
import io.onedev.server.search.entity.QueryBuildContext;

public class DiscardedByMeCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		Path<User> attribute = PullRequestQuery.getPath(context.getRoot(), PullRequestConstants.ATTR_CLOSE_USER);
		return context.getBuilder().equal(attribute, user);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return Objects.equals(request.getSubmitter(), user);
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
