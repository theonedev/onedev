package io.onedev.server.job.resource;

import java.util.Map;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.search.entity.agent.AgentQuery;

public interface ResourceManager {
	
	void run(Runnable runnable, Map<String, Integer> serverResourceRequirements, TaskLogger logger);
	
	void run(AgentAwareRunnable runnable, Map<String, Integer> serverResourceRequirements, 
			AgentQuery agentQuery, Map<String, Integer> agentResourceRequirements, 
			TaskLogger logger);
	
	void waitingForAgentResourceToBeReleased(Long agentId);
	
}
