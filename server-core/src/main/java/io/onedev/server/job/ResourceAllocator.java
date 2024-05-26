package io.onedev.server.job;

import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.search.entity.agent.AgentQuery;

public interface ResourceAllocator {

	boolean runServerJob(String executorName, int totalConcurrency, int requiredConcurrency, 
					  ClusterTask<Boolean> runnable);

	boolean runAgentJob(AgentQuery agentQuery, String executorName, int totalConcurrency,
								int requiredConcurrency, AgentRunnable runnable);
	
	void agentDisconnecting(Long agentId);

}
