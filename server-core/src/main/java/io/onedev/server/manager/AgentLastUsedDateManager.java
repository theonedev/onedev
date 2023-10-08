package io.onedev.server.manager;

import io.onedev.server.model.AgentLastUsedDate;
import io.onedev.server.persistence.dao.EntityManager;

public interface AgentLastUsedDateManager extends EntityManager<AgentLastUsedDate> {

	void create(AgentLastUsedDate lastUsedDate);
	
}
