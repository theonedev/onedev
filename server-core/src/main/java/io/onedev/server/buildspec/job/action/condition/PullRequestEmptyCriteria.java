package io.onedev.server.buildspec.job.action.condition;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class PullRequestEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	public PullRequestEmptyCriteria(int operator) {
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		var matches = build.getRequest() == null;
		if (operator == ActionConditionLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_PULL_REQUEST) + " " + ActionCondition.getRuleName(operator);
	}

}
