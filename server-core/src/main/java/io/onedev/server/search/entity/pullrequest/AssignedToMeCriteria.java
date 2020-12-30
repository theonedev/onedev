package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class AssignedToMeCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		if (User.get() != null) {
			Join<?, ?> join = root.join(PullRequest.PROP_ASSIGNMENTS, JoinType.LEFT);
			Path<?> userPath = EntityQuery.getPath(join, PullRequestAssignment.PROP_USER);
			join.on(builder.equal(userPath, User.get())); 
			return join.isNotNull();
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(PullRequest request) {
		User user = User.get();
		if (user != null)
			return request.getAssignments().stream().anyMatch(it->it.getUser().equals(user));
		else 
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.AssignedToMe);
	}

}
