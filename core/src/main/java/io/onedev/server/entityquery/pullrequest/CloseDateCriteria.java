package io.onedev.server.entityquery.pullrequest;

import java.util.Date;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryLexer;

public class CloseDateCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public CloseDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<Date> attribute = PullRequestQuery.getPath(context.getRoot(), PullRequest.FIELD_PATHS.get(PullRequest.FIELD_CLOSE_DATE));
		if (operator == PullRequestQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value);
		else
			return context.getBuilder().greaterThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.IsBefore)
			return request.getLastActivity().getDate().before(value);
		else
			return request.getLastActivity().getDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequest.FIELD_CLOSE_DATE) + " " + PullRequestQuery.getRuleName(operator) + " " + PullRequestQuery.quote(rawValue);
	}

}
