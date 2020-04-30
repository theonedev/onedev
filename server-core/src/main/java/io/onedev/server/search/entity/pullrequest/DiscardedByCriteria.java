package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.search.entity.EntityCriteria;

public class DiscardedByCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public DiscardedByCriteria(User user) {
		this.user = user;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<User> attribute = PullRequestQuery.getPath(root, PullRequest.PROP_CLOSE_INFO + "." + CloseInfo.PROP_USER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(PullRequest request) {
		return Objects.equals(request.getSubmitter(), user);
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.DiscardedBy) + " " + quote(user.getName());
	}

}
