package io.onedev.server.event.agent;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.event.Event;
import io.onedev.server.model.Agent;

public abstract class AgentEvent extends Event {
	
	private final Long agentId;
	
	public AgentEvent(Agent agent) {
		agentId = agent.getId();
	}

	public Agent getAgent() {
		return OneDev.getInstance(AgentManager.class).load(agentId);
	}
	
}
