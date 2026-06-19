package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class NumberCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Long number;
		
	public NumberCriteria(Long number, int operator) {
		this.operator = operator;
		this.number = number;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(PullRequest.PROP_NUMBER);
		Predicate predicate;
		if (operator == PullRequestQueryLexer.Is)
			predicate = builder.equal(attribute, number);
		else if (operator == PullRequestQueryLexer.IsNot)
			predicate = builder.not(builder.equal(attribute, number));
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			predicate = builder.greaterThan(attribute, number);
		else
			predicate = builder.lessThan(attribute, number);
		return predicate;
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.Is)
			return request.getNumber() == number;
		else if (operator == PullRequestQueryLexer.IsNot)
			return request.getNumber() != number;
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return request.getNumber() > number;
		else
			return request.getNumber() < number;
	}

	@Override
	public String toStringWithoutParens() {
		return "#" + number;
	}

}
