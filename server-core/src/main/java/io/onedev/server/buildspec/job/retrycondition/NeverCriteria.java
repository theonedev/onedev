package io.onedev.server.buildspec.job.retrycondition;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class NeverCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(Build build) {
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return RetryCondition.getRuleName(RetryConditionLexer.Never);
	}

}
