package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class SmileCountCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public SmileCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Integer> attribute = from.get(Issue.PROP_SMILE_COUNT);
		if (operator == IssueQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == IssueQueryLexer.IsNot)
			return builder.not(builder.equal(attribute, value));
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getSmileCount() == value;
		else if (operator == IssueQueryLexer.IsNot)
			return issue.getSmileCount() != value;
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getSmileCount() > value;
		else
			return issue.getSmileCount() < value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_SMILE_COUNT) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}

} 