package io.onedev.server.buildspec.job.retrycondition;

import io.onedev.server.model.Build;

public class RetryContext {

	private final Build build;
	
	private final String errorMessage;
	
	public RetryContext(Build build, String errorMessage) {
		this.build = build;
		this.errorMessage = errorMessage;
	}

	public Build getBuild() {
		return build;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
}
