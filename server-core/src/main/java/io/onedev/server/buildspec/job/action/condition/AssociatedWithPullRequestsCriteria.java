package io.onedev.server.buildspec.job.action.condition;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class AssociatedWithPullRequestsCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Build build) {
		return build.getRequest() != null;
	}

	@Override
	public String toStringWithoutParens() {
		return ActionCondition.getRuleName(ActionConditionLexer.AssociatedWithPullRequests);
	}

}
