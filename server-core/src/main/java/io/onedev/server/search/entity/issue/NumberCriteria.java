package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class NumberCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Long number;
		
	public NumberCriteria(Long number, int operator) {
		this.operator = operator;
		this.number = number;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Issue.PROP_NUMBER);
		Predicate predicate;
		if (operator == IssueQueryLexer.Is)
			predicate = builder.equal(attribute, number);
		else if (operator == IssueQueryLexer.IsNot)
			predicate = builder.not(builder.equal(attribute, number));
		else if (operator == IssueQueryLexer.IsGreaterThan)
			predicate = builder.greaterThan(attribute, number);
		else
			predicate = builder.lessThan(attribute, number);
		return predicate;
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getNumber() == number;
		else if (operator == IssueQueryLexer.IsNot)
			return issue.getNumber() != number;
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getNumber() > number;
		else
			return issue.getNumber() < number;
	}

	@Override
	public String toStringWithoutParens() {
		return "#" + number;
	}

}
