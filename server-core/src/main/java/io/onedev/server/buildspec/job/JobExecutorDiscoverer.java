package io.onedev.server.buildspec.job;

import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

public interface JobExecutorDiscoverer {
	
	JobExecutor discover();

	int getOrder();
    
}
