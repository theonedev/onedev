package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;

public class SubmittedByUserCriteria extends SubmittedByCriteria {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public SubmittedByUserCriteria(User user) {
		this.user = user;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<User> attribute = from.get(PullRequest.PROP_SUBMITTER);
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
