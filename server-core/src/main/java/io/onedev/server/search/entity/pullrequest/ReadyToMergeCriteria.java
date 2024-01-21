package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class ReadyToMergeCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return getCriteria().getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(PullRequest request) {
		return getCriteria().matches(request);
	}

	private Criteria<PullRequest> getCriteria() {
		return new AndCriteria<>(
				new OpenCriteria(),
				new NotCriteria<>(new OrCriteria<>(
						new HasMergeConflictsCriteria(),
						new HasPendingReviewsCriteria(),
						new SomeoneRequestedForChangesCriteria(),
						new HasUnsuccessfulBuilds(),
						new HasUnfinishedBuildsCriteria())));
	}
	
	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ReadyToMerge);
	}

}
