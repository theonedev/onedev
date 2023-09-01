package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequest.Status;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class StatusCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private Status status;
	
	public StatusCriteria(Status status) {
		this.status = status;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return builder.equal(from.get(PullRequest.PROP_STATUS), status);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getStatus() == status;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_STATUS) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ quote(status.toString());
	}

}
