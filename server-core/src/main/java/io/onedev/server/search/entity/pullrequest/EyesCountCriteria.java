package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class EyesCountCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public EyesCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Integer> attribute = from.get(PullRequest.PROP_EYES_COUNT);
		if (operator == PullRequestQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == PullRequestQueryLexer.IsNot)
			return builder.not(builder.equal(attribute, value));
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.Is)
			return request.getEyesCount() == value;
		else if (operator == PullRequestQueryLexer.IsNot)
			return request.getEyesCount() != value;
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return request.getEyesCount() > value;
		else
			return request.getEyesCount() < value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_EYES_COUNT) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}

} 