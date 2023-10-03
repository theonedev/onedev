package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

import static io.onedev.server.model.Issue.NAME_ESTIMATED_TIME;
import static io.onedev.server.model.Issue.PROP_TOTAL_ESTIMATED_TIME;


public class EstimatedTimeCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final int value;
	
	private final int operator;
	
	public EstimatedTimeCriteria(int value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Integer> estimatedTimeAttribute = from.get(PROP_TOTAL_ESTIMATED_TIME);
		if (operator == IssueQueryLexer.Is)
			return builder.equal(estimatedTimeAttribute, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return builder.greaterThan(estimatedTimeAttribute, value);
		else
			return builder.lessThan(estimatedTimeAttribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getTotalEstimatedTime() == value;
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getTotalEstimatedTime() > value;
		else
			return issue.getTotalEstimatedTime() < value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(NAME_ESTIMATED_TIME) + " "
				+ IssueQuery.getRuleName(operator) + " "
				+ quote(DateUtils.formatWorkingPeriod(value));
	}

	@Override
	public void fill(Issue issue) {
	}

}
