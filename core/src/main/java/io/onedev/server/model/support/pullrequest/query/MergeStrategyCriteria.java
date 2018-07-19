package io.onedev.server.model.support.pullrequest.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.util.query.QueryBuildContext;

public class MergeStrategyCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private MergeStrategy value;
	
	public MergeStrategyCriteria(MergeStrategy value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<?> attribute = context.getRoot().get(PullRequest.FIELD_MERGE_STRATEGY);
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getMergeStrategy() == value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequest.FIELD_MERGE_STRATEGY) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " + PullRequestQuery.quote(value.toString());
	}

}
