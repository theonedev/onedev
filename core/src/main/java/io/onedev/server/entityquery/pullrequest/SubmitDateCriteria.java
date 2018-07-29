package io.onedev.server.entityquery.pullrequest;

import java.util.Date;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryLexer;

public class SubmitDateCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public SubmitDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<Date> attribute = context.getRoot().get(PullRequest.FIELD_PATHS.get(PullRequest.FIELD_SUBMIT_DATE));
		if (operator == PullRequestQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value);
		else
			return context.getBuilder().greaterThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.IsBefore)
			return request.getSubmitDate().before(value);
		else
			return request.getSubmitDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequest.FIELD_SUBMIT_DATE) + " " + PullRequestQuery.getRuleName(operator) + " " + PullRequestQuery.quote(rawValue);
	}

}
