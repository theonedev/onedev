package io.onedev.server.ci.job.retry.condition;

import java.util.function.Predicate;

import io.onedev.server.model.Build;

public class AlwaysCriteria implements Predicate<Build> {

	@Override
	public boolean test(Build build) {
		return true;
	}

}
