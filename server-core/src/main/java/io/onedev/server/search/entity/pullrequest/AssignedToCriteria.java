package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;

public class AssignedToCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public AssignedToCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(PullRequest.PROP_ASSIGNMENTS, JoinType.LEFT);
		Path<?> userPath = EntityQuery.getPath(join, PullRequestAssignment.PROP_USER);
		join.on(builder.equal(userPath, user));
		return join.isNotNull();
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getAssignments().stream().anyMatch(it->it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.AssignedTo) + " " 
				+ quote(user.getName());
	}

}
