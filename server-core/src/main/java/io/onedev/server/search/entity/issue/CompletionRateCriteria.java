package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

import static io.onedev.server.model.Issue.*;


public class CompletionRateCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final float value;
	
	private final int operator;
	
	public CompletionRateCriteria(float value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Integer> completionRateAttribute = from.get(PROP_COMPLETION_RATE);
		Predicate ratePredicate;
		int intValue = (int)(value*100);
		if (operator == IssueQueryLexer.IsGreaterThan)
			ratePredicate = builder.gt(completionRateAttribute, intValue);
		else
			ratePredicate = builder.lt(completionRateAttribute, intValue);
		return builder.and(builder.not(builder.equal(completionRateAttribute, -1)), ratePredicate);
	}

	@Override
	public boolean matches(Issue issue) {
		if (issue.getCompletionRate() != -1) {
			int intValue = (int)(value*100);
			if (operator == IssueQueryLexer.IsGreaterThan)
				return issue.getCompletionRate() > intValue;
			else
				return issue.getCompletionRate() < intValue;
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return quote(NAME_COMPLETION_RATE) + " "
				+ IssueQuery.getRuleName(operator) + " "
				+ quote(String.format("%.2f", value));
	}

	@Override
	public void fill(Issue issue) {
	}

}
