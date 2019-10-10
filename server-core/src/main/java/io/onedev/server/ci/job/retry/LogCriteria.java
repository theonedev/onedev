package io.onedev.server.ci.job.retry;

import java.util.regex.Pattern;

import io.onedev.server.OneDev;
import io.onedev.server.ci.job.log.LogManager;
import io.onedev.server.model.Build;

public class LogCriteria extends Criteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public LogCriteria(String value) {
		this.value = value;
	}
	
	@Override
	public boolean satisfied(Build build) {
		Pattern pattern = Pattern.compile(value);
		return OneDev.getInstance(LogManager.class).matches(build, pattern);
	}

}
