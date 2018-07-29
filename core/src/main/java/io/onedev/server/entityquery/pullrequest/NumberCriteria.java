package io.onedev.server.entityquery.pullrequest;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryLexer;

public class NumberCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final long value;
	
	public NumberCriteria(long value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<Long> attribute = context.getRoot().get(PullRequest.FIELD_PATHS.get(PullRequest.FIELD_NUMBER));
		if (operator == PullRequestQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return context.getBuilder().greaterThan(attribute, value);
		else
			return context.getBuilder().lessThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.Is)
			return request.getNumber() == value;
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return request.getNumber() > value;
		else
			return request.getNumber() < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequest.FIELD_NUMBER) + " " + PullRequestQuery.getRuleName(operator) + " " + PullRequestQuery.quote(String.valueOf(value));
	}

}
