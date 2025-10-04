package io.onedev.server.service;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;

import java.util.Collection;
import java.util.Map;

public interface AgentAttributeService extends EntityService<AgentAttribute> {

	void create(AgentAttribute attribute);
	
	Collection<String> getAttributeNames();

	void syncAttributes(Agent agent, Map<String, String> attributeMap);
	
}
