package io.onedev.server.event.agent;

import java.util.Date;

import io.onedev.server.event.Event;
import io.onedev.server.model.Agent;

public abstract class AgentEvent extends Event {
	
	private final Agent agent;
	
	public AgentEvent(Agent agent) {
		super(null, new Date());
		this.agent = agent;
	}

	public Agent getAgent() {
		return agent;
	}
	
}
