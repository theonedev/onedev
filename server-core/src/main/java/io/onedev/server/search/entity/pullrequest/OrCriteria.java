package io.onedev.server.search.entity.pullrequest;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.OrCriteriaHelper;
import io.onedev.server.search.entity.ParensAware;

public class OrCriteria extends EntityCriteria<PullRequest> implements ParensAware {
	
	private static final long serialVersionUID = 1L;

	private final List<EntityCriteria<PullRequest>> criterias;
	
	public OrCriteria(List<EntityCriteria<PullRequest>> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		return new OrCriteriaHelper<PullRequest>(criterias).getPredicate(root, builder);
	}

	@Override
	public boolean matches(PullRequest request) {
		return new OrCriteriaHelper<PullRequest>(criterias).matches(request);
	}

	@Override
	public String toString() {
		return new OrCriteriaHelper<PullRequest>(criterias).toString();
	}
	
}
