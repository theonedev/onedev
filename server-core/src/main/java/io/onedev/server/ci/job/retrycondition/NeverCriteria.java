package io.onedev.server.ci.job.retrycondition;

import java.util.function.Predicate;

import io.onedev.server.model.Build;

public class NeverCriteria implements Predicate<Build> {

	@Override
	public boolean test(Build build) {
		return false;
	}

}
