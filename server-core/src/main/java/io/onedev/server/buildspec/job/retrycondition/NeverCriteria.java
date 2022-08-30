package io.onedev.server.buildspec.job.retrycondition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.util.criteria.Criteria;

public class NeverCriteria extends Criteria<RetryContext> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<RetryContext, RetryContext> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(RetryContext context) {
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return RetryCondition.getRuleName(RetryConditionLexer.Never);
	}

}
