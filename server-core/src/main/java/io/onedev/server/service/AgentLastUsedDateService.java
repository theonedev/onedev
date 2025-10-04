package io.onedev.server.service;

import io.onedev.server.model.AgentLastUsedDate;

public interface AgentLastUsedDateService extends EntityService<AgentLastUsedDate> {

	void create(AgentLastUsedDate lastUsedDate);
	
}
