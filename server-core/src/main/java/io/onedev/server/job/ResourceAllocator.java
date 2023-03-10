package io.onedev.server.job;

import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.search.entity.agent.AgentQuery;

public interface ResourceAllocator {

	void runServerJob(String resourceType, int totalResources, int requiredResources, 
					  ClusterRunnable runnable);

	void runAgentJob(AgentQuery agentQuery, String resourceType, int totalResources, 
					 int requiredResources, AgentRunnable runnable);
	
	void agentDisconnecting(Long agentId);

}
