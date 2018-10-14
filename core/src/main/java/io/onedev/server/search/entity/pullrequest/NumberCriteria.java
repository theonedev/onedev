package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.PullRequestConstants;

public class NumberCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final long value;
	
	public NumberCriteria(long value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		Path<Long> attribute = context.getRoot().get(PullRequestConstants.ATTR_NUMBER);
		if (operator == PullRequestQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return context.getBuilder().greaterThan(attribute, value);
		else
			return context.getBuilder().lessThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
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
		return PullRequestQuery.quote(PullRequestConstants.FIELD_NUMBER) + " " + PullRequestQuery.getRuleName(operator) + " " + PullRequestQuery.quote(String.valueOf(value));
	}

}
