package io.onedev.server.model.support.pullrequest.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.util.query.QueryBuildContext;

public class StateCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	public StateCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		Path<?> attribute = PullRequestQuery.getPath(context.getRoot(), PullRequest.PATH_CLOSE_STATUS);
		if (value.equalsIgnoreCase(PullRequest.STATE_OPEN)) 
			return context.getBuilder().isNull(attribute);
		else
			return context.getBuilder().equal(attribute, CloseInfo.Status.valueOf(value.toUpperCase()));
	}

	@Override
	public boolean matches(PullRequest request) {
		if (value.equalsIgnoreCase(PullRequest.STATE_OPEN)) 
			return request.getCloseInfo() == null;
		else
			return request.getCloseInfo() != null && request.getCloseInfo().getStatus().name().equalsIgnoreCase(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequest.FIELD_STATE) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " + PullRequestQuery.quote(value);
	}

}
