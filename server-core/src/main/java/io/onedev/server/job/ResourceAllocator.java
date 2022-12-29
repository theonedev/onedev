package io.onedev.server.job;

import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.search.entity.agent.AgentQuery;

public interface ResourceAllocator {

	void runServerJob(String resourceHolder, int total, int required, ClusterRunnable runnable);

	void runAgentJob(AgentQuery agentQuery, String resourceHolder, int total, int required, 
					 AgentRunnable runnable);
	
	void wantToDisconnectAgent(Long agentId);
	
}
