package io.onedev.server.ci.job.action.condition;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.onedev.server.model.Build;

public class ErrorMessageCriteria implements Predicate<Build> {

	private final String value;
	
	public ErrorMessageCriteria(String value) {
		this.value = value;
	}

	@Override
	public boolean test(Build build) {
		return Pattern.compile(value).matcher(build.getStatusMessage()).find();
	}

}
