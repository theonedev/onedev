package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.PullRequestConstants;
import io.onedev.server.search.entity.QueryBuildContext;

public class DiscardedByCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final User value;
	
	public DiscardedByCriteria(User value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		Path<User> attribute = PullRequestQuery.getPath(context.getRoot(), PullRequestConstants.ATTR_CLOSE_USER);
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request, User usert) {
		return Objects.equals(request.getSubmitter(), value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.DiscardedBy) + " " + PullRequestQuery.quote(value.getName());
	}

}
