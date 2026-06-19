package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class NumberCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Long number;
		
	public NumberCriteria(Long number, int operator) {
		this.operator = operator;
		this.number = number;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Build.PROP_NUMBER);
		Predicate predicate;
		if (operator == BuildQueryLexer.Is)
			predicate = builder.equal(attribute, number);
		else if (operator == BuildQueryLexer.IsNot)
			predicate = builder.not(builder.equal(attribute, number));
		else if (operator == BuildQueryLexer.IsGreaterThan)
			predicate = builder.greaterThan(attribute, number);
		else
			predicate = builder.lessThan(attribute, number);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		if (operator == BuildQueryLexer.Is)
			return build.getNumber() == number;
		else if (operator == BuildQueryLexer.IsNot)
			return build.getNumber() != number;
		else if (operator == BuildQueryLexer.IsGreaterThan)
			return build.getNumber() > number;
		else
			return build.getNumber() < number;
	}

	@Override
	public String toStringWithoutParens() {
		return "#" + number;
	}

}
