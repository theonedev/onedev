package io.onedev.server.search.entity.pullrequest;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.criteria.Criteria;

public class NumberCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final ProjectScopedNumber number;
	
	public NumberCriteria(@Nullable Project project, String value, int operator) {
		this.operator = operator;
		this.value = value;
		number = EntityQuery.getProjectScopedNumber(project, value);
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(PullRequest.PROP_NUMBER);
		Predicate numberPredicate;
		
		if (operator == PullRequestQueryLexer.Is)
			numberPredicate = builder.equal(attribute, number.getNumber());
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			numberPredicate = builder.greaterThan(attribute, number.getNumber());
		else
			numberPredicate = builder.lessThan(attribute, number.getNumber());
		
		return builder.and(
				builder.equal(from.get(PullRequest.PROP_TARGET_PROJECT), number.getProject()),
				numberPredicate);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (request.getTargetProject().equals(number.getProject())) {
			if (operator == PullRequestQueryLexer.Is)
				return request.getNumber() == number.getNumber();
			else if (operator == PullRequestQueryLexer.IsGreaterThan)
				return request.getNumber() > number.getNumber();
			else
				return request.getNumber() < number.getNumber();
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_NUMBER) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
