package io.onedev.server.event.agent;

import io.onedev.server.model.Agent;

public abstract class AgentEvent {
	
	private final Agent agent;
	
	public AgentEvent(Agent agent) {
		this.agent = agent;
	}

	public Agent getAgent() {
		return agent;
	}
	
}
