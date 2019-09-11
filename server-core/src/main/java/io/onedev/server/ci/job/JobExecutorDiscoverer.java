package io.onedev.server.ci.job;

import javax.annotation.Nullable;

import io.onedev.server.model.support.jobexecutor.JobExecutor;

public interface JobExecutorDiscoverer {
	
	@Nullable
	JobExecutor discover();
	
}
