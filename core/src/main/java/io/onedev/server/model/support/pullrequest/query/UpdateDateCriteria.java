package io.onedev.server.model.support.pullrequest.query;

import java.util.Date;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.query.QueryBuildContext;

public class UpdateDateCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public UpdateDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<Long> attribute = PullRequestQuery.getPath(context.getRoot(), PullRequest.FIELD_PATHS.get(PullRequest.FIELD_UPDATE_DATE));
		if (operator == PullRequestQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value.getTime());
		else
			return context.getBuilder().greaterThan(attribute, value.getTime());
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
		return PullRequestQuery.quote(PullRequest.FIELD_UPDATE_DATE) + " " + PullRequestQuery.getRuleName(operator) + " " + PullRequestQuery.quote(rawValue);
	}

}
