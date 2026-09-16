package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;

public class ToBeChangedByUserCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public ToBeChangedByUserCriteria(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return getCriteria(user).getPredicate(projectScope, query, from, builder);
	}

	@Override
	public boolean matches(PullRequest request) {
		return getCriteria(user).matches(request);
	}

	@SuppressWarnings("unchecked")
	private Criteria<PullRequest> getCriteria(User user) {
		return new AndCriteria<>(
				new OpenCriteria(),
				new SubmittedByUserCriteria(user), 
				new SomeoneRequestedForChangesCriteria());
	}
	
	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeChangedBy) + " " + quote(user.getName());
	}

}
