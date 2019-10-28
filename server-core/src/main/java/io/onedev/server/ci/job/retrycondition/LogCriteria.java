package io.onedev.server.ci.job.retrycondition;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.onedev.server.OneDev;
import io.onedev.server.ci.job.log.LogManager;
import io.onedev.server.model.Build;

public class LogCriteria implements Predicate<Build> {

	private final String value;
	
	public LogCriteria(String value) {
		this.value = value;
	}
	
	@Override
	public boolean test(Build build) {
		Pattern pattern = Pattern.compile(value);
		return OneDev.getInstance(LogManager.class).matches(build, pattern);
	}

}
