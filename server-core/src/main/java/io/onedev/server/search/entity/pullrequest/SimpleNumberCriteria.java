package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class SimpleNumberCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;
	
	private final long value;
	
	public SimpleNumberCriteria(long value) {
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return builder.equal(from.get(PullRequest.PROP_NUMBER), value);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getNumber() == value;
	}

	@Override
	public String toStringWithoutParens() {
		return "#" + value;
	}

}
