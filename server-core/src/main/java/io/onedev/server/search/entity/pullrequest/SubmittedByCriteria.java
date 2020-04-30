package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;

public class SubmittedByCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public SubmittedByCriteria(User user) {
		this.user = user;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<User> attribute = root.get(PullRequest.PROP_SUBMITTER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(PullRequest request) {
		return Objects.equals(request.getSubmitter(), user);
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.SubmittedBy) + " " + quote(user.getName());
	}

}
