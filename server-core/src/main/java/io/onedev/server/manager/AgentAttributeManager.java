package io.onedev.server.manager;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;
import java.util.Map;

public interface AgentAttributeManager extends EntityManager<AgentAttribute> {

	void create(AgentAttribute attribute);
	
	Collection<String> getAttributeNames();

	void syncAttributes(Agent agent, Map<String, String> attributeMap);
	
}
