package io.onedev.server.buildspec.job.action.condition;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class PullRequestIsEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Build build) {
		return build.getRequest() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_PULL_REQUEST) + " " + ActionCondition.getRuleName(ActionConditionLexer.IsEmpty);
	}

}
