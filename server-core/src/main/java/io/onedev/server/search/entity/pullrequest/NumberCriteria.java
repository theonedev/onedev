package io.onedev.server.search.entity.pullrequest;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScopedNumber;

public class NumberCriteria extends EntityCriteria<PullRequest> {

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
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<Long> attribute = root.get(PullRequest.PROP_NUMBER);
		Predicate numberPredicate;
		
		if (operator == PullRequestQueryLexer.Is)
			numberPredicate = builder.equal(attribute, number.getNumber());
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			numberPredicate = builder.greaterThan(attribute, number.getNumber());
		else
			numberPredicate = builder.lessThan(attribute, number.getNumber());
		
		return builder.and(
				builder.equal(root.get(PullRequest.PROP_TARGET_PROJECT), number.getProject()),
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
