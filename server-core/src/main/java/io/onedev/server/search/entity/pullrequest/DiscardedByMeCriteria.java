package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class DiscardedByMeCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder, User user) {
		if (user != null) {
			Path<User> attribute = PullRequestQuery.getPath(root, PullRequestQueryConstants.ATTR_CLOSE_USER);
			return builder.equal(attribute, user);
		} else {
			return builder.disjunction();
		}
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		if (user != null)
			return user.equals(request.getSubmitter());
		else
			return false;
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
