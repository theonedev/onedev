package io.onedev.server.search.entity.pullrequest;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequest.Status;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class StatusCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Status status;
	
	private final int operator;
	
	public StatusCriteria(Status status, int operator) {
		this.status = status;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		var predicate = builder.equal(from.get(PullRequest.PROP_STATUS), status);
		if (operator == PullRequestQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(PullRequest request) {
		var matches = request.getStatus() == status;
		if (operator == PullRequestQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_STATUS) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(status.toString());
	}

}
