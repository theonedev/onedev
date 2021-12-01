package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.search.entity.EntityCriteria;

public class MergedCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private EntityCriteria<PullRequest> getCriteria() {
		return new StatusCriteria(CloseInfo.Status.MERGED.toString());
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return getCriteria().getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(PullRequest request) {
		return getCriteria().matches(request);
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.Merged);
	}

}
