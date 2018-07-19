package io.onedev.server.model.support.pullrequest.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.query.QueryBuildContext;

public class DescriptionCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<String> attribute = context.getRoot().get(PullRequest.FIELD_PATHS.get(PullRequest.FIELD_DESCRIPTION));
		return context.getBuilder().like(attribute, "%" + value + "%");
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getDescription().toLowerCase().contains(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequest.FIELD_DESCRIPTION) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " + PullRequestQuery.quote(value);
	}

}
