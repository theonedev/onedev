package io.onedev.server.buildspec.job.retrycondition;

import io.onedev.server.model.Build;

import org.jspecify.annotations.Nullable;

public class RetryContext {

	private final Build build;
	
	private final String errorMessage;
	
	private final boolean timedOut;

	public RetryContext(Build build, @Nullable String errorMessage, boolean timedOut) {
		this.build = build;
		this.errorMessage = errorMessage;
		this.timedOut = timedOut;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public Build getBuild() {
		return build;
	}

	@Nullable
	public String getErrorMessage() {
		return errorMessage;
	}
	
}
