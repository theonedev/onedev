package io.onedev.server.search.entity.issue;

import static io.onedev.server.model.Issue.NAME_PROGRESS;
import static io.onedev.server.model.Issue.PROP_PROGRESS;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;


public class ProgressCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final float value;
	
	private final int operator;
	
	public ProgressCriteria(float value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
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
