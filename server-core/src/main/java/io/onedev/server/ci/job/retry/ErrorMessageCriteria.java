package io.onedev.server.ci.job.retry;

import java.util.regex.Pattern;

import io.onedev.server.model.Build;

public class ErrorMessageCriteria extends Criteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ErrorMessageCriteria(String value) {
		this.value = value;
	}
	
	@Override
	public boolean satisfied(Build build) {
		return Pattern.compile(value).matcher(build.getStatusMessage()).find();
	}

}
