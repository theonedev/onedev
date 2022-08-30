package io.onedev.server.buildspec.job.retrycondition;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.util.criteria.Criteria;

public class ParamCriteria extends Criteria<RetryContext> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;
	
	public ParamCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<RetryContext, RetryContext> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(RetryContext context) {
		List<String> paramValues = context.getBuild().getParamMap().get(name);
		return paramValues != null && paramValues.contains(value);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " 
				+ RetryCondition.getRuleName(RetryConditionLexer.Is) + " "
				+ quote(value);
	}
	
}
