package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class AssignedToCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public AssignedToCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestAssignment> assignmentQuery = query.subquery(PullRequestAssignment.class);
		Root<PullRequestAssignment> assignment = assignmentQuery.from(PullRequestAssignment.class);
		assignmentQuery.select(assignment);
		assignmentQuery.where(builder.and(
				builder.equal(assignment.get(PullRequestAssignment.PROP_REQUEST), from),
				builder.equal(assignment.get(PullRequestAssignment.PROP_USER), user)));
		return builder.exists(assignmentQuery);
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
