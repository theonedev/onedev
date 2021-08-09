package io.onedev.server.entitymanager;

import java.util.List;
import java.util.Map;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentAttribute;
import io.onedev.server.persistence.dao.EntityManager;

public interface AgentAttributeManager extends EntityManager<AgentAttribute> {
	
	List<String> getAttributeNames();

	void syncAttributes(Agent agent, Map<String, String> attributeMap);
	
}
