package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class DiscardedByCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final User value;
	
	public DiscardedByCriteria(User value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<User> attribute = PullRequestQuery.getPath(root, PullRequestConstants.ATTR_CLOSE_USER);
		return builder.equal(attribute, value);
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
