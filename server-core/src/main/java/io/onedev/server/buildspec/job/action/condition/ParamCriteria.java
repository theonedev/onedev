package io.onedev.server.buildspec.job.action.condition;

import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ParamCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String value;
	
	private final int operator;
	
	public ParamCriteria(String name, String value, int operator) {
		this.name = name;
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		var matches = paramValues != null && paramValues.contains(value);
		if (operator == ActionConditionLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " 
				+ ActionCondition.getRuleName(operator) + " "
				+ quote(value);
	}
	
}
