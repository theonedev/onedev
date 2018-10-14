package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.PullRequestConstants;

public class StatusCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	public StatusCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context, User user) {
		Path<?> attribute = PullRequestQuery.getPath(context.getRoot(), PullRequestConstants.ATTR_CLOSE_STATUS);
		if (value.equalsIgnoreCase(PullRequestConstants.STATE_OPEN)) 
			return context.getBuilder().isNull(attribute);
		else
			return context.getBuilder().equal(attribute, CloseInfo.Status.valueOf(value.toUpperCase()));
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		if (value.equalsIgnoreCase(PullRequestConstants.STATE_OPEN)) 
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
		return PullRequestQuery.quote(PullRequestConstants.FIELD_STATUS) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " + PullRequestQuery.quote(value);
	}

}
