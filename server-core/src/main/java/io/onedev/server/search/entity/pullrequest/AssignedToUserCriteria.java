package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;

public class AssignedToUserCriteria extends AssignedToCriteria {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public AssignedToUserCriteria(User user) {
		this.user = user;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
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
