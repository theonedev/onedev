package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

import static io.onedev.server.model.Issue.*;


public class ProgressCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final float value;
	
	private final int operator;
	
	public ProgressCriteria(float value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Integer> progressAttribute = from.get(PROP_PROGRESS);
		Predicate ratePredicate;
		int intValue = (int)(value*100);
		if (operator == IssueQueryLexer.IsGreaterThan)
			ratePredicate = builder.gt(progressAttribute, intValue);
		else
			ratePredicate = builder.lt(progressAttribute, intValue);
		return builder.and(builder.not(builder.equal(progressAttribute, -1)), ratePredicate);
	}

	@Override
	public boolean matches(Issue issue) {
		if (issue.getProgress() != -1) {
			int intValue = (int)(value*100);
			if (operator == IssueQueryLexer.IsGreaterThan)
				return issue.getProgress() > intValue;
			else
				return issue.getProgress() < intValue;
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return quote(NAME_PROGRESS) + " "
				+ IssueQuery.getRuleName(operator) + " "
				+ quote(String.format("%.2f", value));
	}

	@Override
	public void fill(Issue issue) {
	}

}
