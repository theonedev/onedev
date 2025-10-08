package io.onedev.server.buildspec.job.retrycondition;

import io.onedev.server.model.Build;

import org.jspecify.annotations.Nullable;

public class RetryContext {

	private final Build build;
	
	private final String errorMessage;
	
	public RetryContext(Build build, @Nullable String errorMessage) {
		this.build = build;
		this.errorMessage = errorMessage;
	}

	public Build getBuild() {
		return build;
	}

	@Nullable
	public String getErrorMessage() {
		return errorMessage;
	}
	
}
