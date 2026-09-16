package io.onedev.server.buildspec.job.retrycondition;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class NeverCriteria extends Criteria<RetryContext> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<RetryContext, RetryContext> from, CriteriaBuilder builder) {
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
