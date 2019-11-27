package io.onedev.server.buildspec.job.action.condition;

import java.util.function.Predicate;

import io.onedev.server.model.Build;

public class CancelledCriteria implements Predicate<Build> {

	@Override
	public boolean test(Build build) {
		return build.getStatus() == Build.Status.CANCELLED;
	}

}
