package io.onedev.server.buildspec.job.action.condition;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class AiPullRequestEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	public AiPullRequestEmptyCriteria(int operator) {
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		var matches = build.getRequest() == null || build.getRequest().getSubmitter().getType() != User.Type.AI;
		if (operator == ActionConditionLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_AI_PULL_REQUEST) + " " + ActionCondition.getRuleName(operator);
	}

}
