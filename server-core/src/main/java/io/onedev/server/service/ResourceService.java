package io.onedev.server.service;

import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.service.support.AgentRunnable;

public interface ResourceService {

	<T> T runServerJob(String resourceName, int totalConcurrency, 
						int requiredConcurrency, ClusterTask<T> runnable);

	<T> T runAgentJob(AgentQuery agentQuery, String resourceName, int totalConcurrency,
						int requiredConcurrency, AgentRunnable<T> runnable);
	
	void agentDisconnecting(Long agentId);

}
