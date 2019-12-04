package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class DiscardedByCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	private final String value;
	
	public DiscardedByCriteria(String value) {
		user = EntityQuery.getUser(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<User> attribute = PullRequestQuery.getPath(root, PullRequestQueryConstants.ATTR_CLOSE_USER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(PullRequest request) {
		return Objects.equals(request.getSubmitter(), user);
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.DiscardedBy) 
				+ " " + PullRequestQuery.quote(value);
	}

}
