package io.onedev.server.buildspec.job;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

public interface JobExecutorDiscoverer {
	
	@Nullable
	JobExecutor discover();
	
	int getOrder();
	
}
