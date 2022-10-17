package io.onedev.server.event.agent;

import io.onedev.server.model.Agent;

public class AgentConnected extends AgentEvent {

	public AgentConnected(Agent agent) {
		super(agent);
	}

}
