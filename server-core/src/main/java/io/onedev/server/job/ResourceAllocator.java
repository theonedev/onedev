package io.onedev.server.job;

import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.search.entity.agent.AgentQuery;

public interface ResourceAllocator {

	boolean runServerJob(String resourceType, int totalResources, int requiredResources, 
					  ClusterTask<Boolean> runnable);

	boolean runAgentJob(AgentQuery agentQuery, String resourceType, int totalResources,
								int requiredResources, AgentRunnable runnable);
	
	void agentDisconnecting(Long agentId);

}
