package io.onedev.server.buildspec.job;

import org.jspecify.annotations.Nullable;

import io.onedev.server.buildspec.ParamSpecAware;

public interface JobAware extends ParamSpecAware {
	
	@Nullable
	Job getJob();
	
}
