package io.onedev.server.model.support.pullrequest.query;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.query.QueryBuildContext;

public class SourceProjectCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final Project value;
	
	public SourceProjectCriteria(Project value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<User> attribute = context.getRoot().get(PullRequest.FIELD_PATHS.get(PullRequest.FIELD_SOURCE_PROJECT));
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request) {
		return Objects.equals(request.getSourceProject(), value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequest.FIELD_SOURCE_PROJECT) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " + PullRequestQuery.quote(value.getName());
	}

}
