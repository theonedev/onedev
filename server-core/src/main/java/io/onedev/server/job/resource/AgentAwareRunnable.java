package io.onedev.server.job.resource;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.AgentData;

public interface AgentAwareRunnable {

	void runOn(Long agentId, Session agentSession, AgentData agentData);
	
}
