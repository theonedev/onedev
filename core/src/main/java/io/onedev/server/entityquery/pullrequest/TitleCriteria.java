package io.onedev.server.entityquery.pullrequest;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryLexer;

public class TitleCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public TitleCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<String> attribute = context.getRoot().get(PullRequest.FIELD_PATHS.get(PullRequest.FIELD_TITLE));
		return context.getBuilder().like(context.getBuilder().lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getTitle().toLowerCase().contains(value.toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequest.FIELD_TITLE) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " + PullRequestQuery.quote(value);
	}

}
