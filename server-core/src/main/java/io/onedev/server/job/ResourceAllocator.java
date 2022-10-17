package io.onedev.server.job;

import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.search.entity.agent.AgentQuery;

public interface ResourceAllocator {
	
	static final String CPU = "cpu";
	
	static final String MEMORY = "memory";

	void run(ResourceRunnable runnable, @Nullable AgentQuery agentQuery, 
			Map<String, Integer> resourceRequirements);
	
	void waitingForAgentResourceToBeReleased(Long agentId);
	
}
