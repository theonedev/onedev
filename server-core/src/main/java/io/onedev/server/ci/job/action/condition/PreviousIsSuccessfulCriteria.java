package io.onedev.server.ci.job.action.condition;

import java.util.function.Predicate;

import io.onedev.server.model.Build;

public class PreviousIsSuccessfulCriteria implements Predicate<Build> {

	@Override
	public boolean test(Build build) {
		return build.getStreamPrevious(null) != null 
				&& build.getStreamPrevious(null).getStatus() == Build.Status.SUCCESSFUL;
	}

}
