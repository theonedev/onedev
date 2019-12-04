package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.NotCriteriaHelper;

public class NotCriteria extends EntityCriteria<PullRequest> {
	
	private static final long serialVersionUID = 1L;

	private final EntityCriteria<PullRequest> criteria;
	
	public NotCriteria(EntityCriteria<PullRequest> criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		return new NotCriteriaHelper<PullRequest>(criteria).getPredicate(root, builder);
	}

	@Override
	public boolean matches(PullRequest request) {
		return new NotCriteriaHelper<PullRequest>(criteria).matches(request);
	}

	@Override
	public String toString() {
		return new NotCriteriaHelper<PullRequest>(criteria).toString();
	}
	
}
