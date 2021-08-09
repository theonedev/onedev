package io.onedev.server.event.agent;

import io.onedev.server.model.Agent;

public class AgentDisconnected extends AgentEvent {
	
	public AgentDisconnected(Agent agent) {
		super(agent);
	}

}
