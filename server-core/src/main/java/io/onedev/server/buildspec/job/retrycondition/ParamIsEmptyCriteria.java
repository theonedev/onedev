package io.onedev.server.buildspec.job.retrycondition;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class ParamIsEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private String name;
	
	public ParamIsEmptyCriteria(String name) {
		this.name = name;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		return paramValues == null || paramValues.isEmpty();
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " + RetryCondition.getRuleName(RetryConditionLexer.IsEmpty);
	}
	
}
