package io.onedev.server.buildspec.job;

import javax.annotation.Nullable;

import io.onedev.server.buildspec.ParamSpecAware;

public interface JobAware extends ParamSpecAware {
	
	@Nullable
	Job getJob();
	
}
